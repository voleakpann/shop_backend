package com.ministore.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Checkout request. Note there is no {@code userEmail}, {@code total} or
 * {@code status} here — those are always set server-side (identity from the JWT,
 * total recomputed from real product prices).
 */
public record CreateOrderRequest(
        @NotBlank String customerName,
        @NotBlank String phone,
        @NotBlank String address,
        @NotBlank String city,
        String stateRegion,
        String zip,
        @Size(max = 1000) String notes,
        @NotEmpty @Valid List<OrderItemRequest> items) {
}
