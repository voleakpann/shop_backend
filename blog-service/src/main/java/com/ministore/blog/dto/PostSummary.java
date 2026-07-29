package com.ministore.blog.dto;

import com.ministore.blog.model.Post;

import java.time.Instant;
import java.util.List;

/**
 * One card in the blog list: everything the list page renders and nothing more.
 * The full {@code content} is deliberately omitted so listing a page of posts
 * doesn't ship every article body over the wire.
 */
public record PostSummary(
        Long id,
        String slug,
        String title,
        String excerpt,
        String category,
        String coverImage,
        String authorName,
        List<String> tags,
        Instant publishedAt) {

    public static PostSummary of(Post post) {
        return new PostSummary(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getCategory(),
                post.getCoverImage(),
                post.getAuthorName(),
                List.copyOf(post.getTags()),
                post.getPublishedAt());
    }
}
