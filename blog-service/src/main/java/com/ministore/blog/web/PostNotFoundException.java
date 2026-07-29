package com.ministore.blog.web;

/** Thrown when a slug or id matches no visible post. */
public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(String slug) {
        super("No post found for slug '" + slug + "'");
    }

    public PostNotFoundException(Long id) {
        super("No post found with id " + id);
    }
}
