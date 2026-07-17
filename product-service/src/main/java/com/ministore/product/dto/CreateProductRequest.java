package com.ministore.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.ArrayList;
import java.util.List;

/**
 * Request body for creating a product. Deliberately does NOT expose {@code id}
 * so a client can't turn a "create" into an overwrite of an existing row.
 */
public record CreateProductRequest(
        @NotBlank String slug,
        @NotBlank String name,
        @PositiveOrZero double price,
        String category,
        List<String> tags,
        String brand,
        String image,
        boolean featured) {

    public List<String> tags() {
        return tags == null ? new ArrayList<>() : tags;
    }
}
