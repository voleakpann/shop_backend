package com.ministore.blog.dto;

import com.ministore.blog.model.Post;

/** Just enough of a post to render a "previous / next article" link. */
public record PostLink(String slug, String title) {

    public static PostLink of(Post post) {
        return new PostLink(post.getSlug(), post.getTitle());
    }
}
