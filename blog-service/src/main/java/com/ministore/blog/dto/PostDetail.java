package com.ministore.blog.dto;

import com.ministore.blog.model.Post;

import java.time.Instant;
import java.util.List;

/**
 * The single-post page in one response: the article plus the surrounding
 * navigation and related posts, so the page needs a single round trip.
 * {@code previous}/{@code next} are null at the ends of the archive.
 */
public record PostDetail(
        Long id,
        String slug,
        String title,
        String excerpt,
        String content,
        String category,
        String coverImage,
        String authorName,
        List<String> tags,
        Instant publishedAt,
        Instant updatedAt,
        PostLink previous,
        PostLink next,
        List<PostSummary> related) {

    public static PostDetail of(Post post, PostLink previous, PostLink next, List<PostSummary> related) {
        return new PostDetail(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getContent(),
                post.getCategory(),
                post.getCoverImage(),
                post.getAuthorName(),
                List.copyOf(post.getTags()),
                post.getPublishedAt(),
                post.getUpdatedAt(),
                previous,
                next,
                related);
    }
}
