package com.ministore.blog.service;

import com.ministore.blog.dto.CategoryCount;
import com.ministore.blog.dto.PageResponse;
import com.ministore.blog.dto.PostDetail;
import com.ministore.blog.dto.PostLink;
import com.ministore.blog.dto.PostRequest;
import com.ministore.blog.dto.PostSummary;
import com.ministore.blog.model.Post;
import com.ministore.blog.repository.PostRepository;
import com.ministore.blog.web.DuplicateSlugException;
import com.ministore.blog.web.PostNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {

    /** Matches the blog list's grid; used when the client sends no size. */
    static final int DEFAULT_PAGE_SIZE = 6;

    /** Upper bound so a hand-crafted ?size=100000 can't ask for the whole table. */
    static final int MAX_PAGE_SIZE = 50;

    private final PostRepository posts;

    public PostService(PostRepository posts) {
        this.posts = posts;
    }

    /**
     * One page of published posts, newest first. The three filters are mutually
     * exclusive and applied in precedence order: category, then tag, then title search.
     */
    @Transactional(readOnly = true)
    public PageResponse<PostSummary> list(String category, String tag, String query,
                                          Integer page, Integer size) {
        Pageable pageable = pageable(page, size);

        Page<Post> result;
        if (isSet(category) && !category.equalsIgnoreCase("All")) {
            result = posts.findByPublishedTrueAndCategoryIgnoreCase(category.trim(), pageable);
        } else if (isSet(tag)) {
            result = posts.findPublishedByTag(tag.trim(), pageable);
        } else if (isSet(query)) {
            result = posts.findByPublishedTrueAndTitleContainingIgnoreCase(query.trim(), pageable);
        } else {
            result = posts.findByPublishedTrue(pageable);
        }
        return PageResponse.of(result, PostSummary::of);
    }

    /** The single-post page: the article plus previous/next links and related posts. */
    @Transactional(readOnly = true)
    public PostDetail getBySlug(String slug) {
        Post post = posts.findBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> new PostNotFoundException(slug));

        PostLink previous = posts
                .findFirstByPublishedTrueAndPublishedAtLessThanOrderByPublishedAtDesc(post.getPublishedAt())
                .map(PostLink::of)
                .orElse(null);
        PostLink next = posts
                .findFirstByPublishedTrueAndPublishedAtGreaterThanOrderByPublishedAtAsc(post.getPublishedAt())
                .map(PostLink::of)
                .orElse(null);
        List<PostSummary> related = posts
                .findTop3ByPublishedTrueAndCategoryIgnoreCaseAndIdNotOrderByPublishedAtDesc(
                        post.getCategory(), post.getId())
                .stream()
                .map(PostSummary::of)
                .toList();

        return PostDetail.of(post, previous, next, related);
    }

    /** Sidebar category list with a post count each. */
    @Transactional(readOnly = true)
    public List<CategoryCount> categories() {
        return posts.countPublishedByCategory();
    }

    /** Admin-only. The author is taken from the JWT so a client can't forge a byline. */
    @Transactional
    public Post create(PostRequest request, String authorName, String authorEmail) {
        if (posts.existsBySlug(request.slug())) {
            throw new DuplicateSlugException(request.slug());
        }

        Post post = new Post(
                request.slug(),
                request.title(),
                request.excerpt(),
                request.content(),
                request.category(),
                request.coverImage(),
                authorName,
                authorEmail,
                tagsOf(request),
                request.published() == null || request.published(),
                request.publishedAt() != null ? request.publishedAt() : Instant.now());
        return posts.save(post);
    }

    /** Admin-only. The original author is kept — editing a post doesn't reassign it. */
    @Transactional
    public Post update(Long id, PostRequest request) {
        Post post = posts.findById(id).orElseThrow(() -> new PostNotFoundException(id));

        if (!post.getSlug().equals(request.slug()) && posts.existsBySlug(request.slug())) {
            throw new DuplicateSlugException(request.slug());
        }

        post.setSlug(request.slug());
        post.setTitle(request.title());
        post.setExcerpt(request.excerpt());
        post.setContent(request.content());
        post.setCategory(request.category());
        post.setCoverImage(request.coverImage());
        post.setTags(tagsOf(request));
        if (request.published() != null) {
            post.setPublished(request.published());
        }
        if (request.publishedAt() != null) {
            post.setPublishedAt(request.publishedAt());
        }
        post.setUpdatedAt(Instant.now());
        return posts.save(post);
    }

    /** Admin-only. 404s on an unknown id rather than reporting a no-op as success. */
    @Transactional
    public void delete(Long id) {
        Post post = posts.findById(id).orElseThrow(() -> new PostNotFoundException(id));
        posts.delete(post);
    }

    private Pageable pageable(Integer page, Integer size) {
        int safePage = page == null || page < 0 ? 0 : page;
        int requested = size == null || size < 1 ? DEFAULT_PAGE_SIZE : size;
        int safeSize = Math.min(requested, MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "publishedAt"));
    }

    /** Hibernate owns the entity's collection, so hand it a fresh mutable list. */
    private static List<String> tagsOf(PostRequest request) {
        return request.tags() == null ? new ArrayList<>() : new ArrayList<>(request.tags());
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }
}
