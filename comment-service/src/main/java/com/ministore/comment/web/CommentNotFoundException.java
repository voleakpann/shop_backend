package com.ministore.comment.web;

/** Thrown when a referenced comment id doesn't exist. */
public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(Long id) {
        super("No such comment: " + id);
    }
}
