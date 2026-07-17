package com.ministore.order.web;

/** Thrown when a checkout line references a slug that product-service doesn't know. */
public class UnknownProductException extends RuntimeException {
    public UnknownProductException(String slug) {
        super("Unknown product slug: " + slug);
    }
}
