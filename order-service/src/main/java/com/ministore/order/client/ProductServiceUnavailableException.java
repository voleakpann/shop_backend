package com.ministore.order.client;

/** Thrown when product-service can't be reached at all (down, network error) — distinct from a 404 "no such product". */
public class ProductServiceUnavailableException extends RuntimeException {
    public ProductServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
