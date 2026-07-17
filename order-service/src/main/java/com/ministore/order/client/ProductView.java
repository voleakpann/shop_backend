package com.ministore.order.client;

/**
 * The slice of a product we care about when pricing an order. Extra fields in
 * the product-service response are ignored (Spring Boot disables
 * FAIL_ON_UNKNOWN_PROPERTIES by default).
 */
public record ProductView(String slug, String name, double price) {
}
