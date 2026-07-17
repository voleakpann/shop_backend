package com.ministore.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * A requested line item. The client only chooses WHAT (slug) and HOW MANY (qty);
 * the price and name are resolved server-side from product-service so they can't
 * be tampered with.
 */
public record OrderItemRequest(
        @NotBlank String slug,
        @Min(1) int qty) {
}
