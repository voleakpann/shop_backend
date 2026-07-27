package com.ministore.comment.web;

/** Thrown when a reply's parentId points at a comment on a different product. */
public class MismatchedParentException extends RuntimeException {
    public MismatchedParentException(Long parentId, String productSlug) {
        super("Comment " + parentId + " does not belong to product '" + productSlug + "'");
    }
}
