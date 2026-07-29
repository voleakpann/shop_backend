package com.ministore.blog.web;

/** Thrown when a create/update would give two posts the same slug. */
public class DuplicateSlugException extends RuntimeException {

    public DuplicateSlugException(String slug) {
        super("A post with slug '" + slug + "' already exists");
    }
}
