package com.ministore.order.web;

/** Thrown when an order id doesn't exist, or doesn't belong to the requesting user. */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("Unknown order: " + id);
    }
}
