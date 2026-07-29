package com.ministore.blog.dto;

/**
 * One row of the blog sidebar's category list, e.g. {"Camera", 4}.
 * {@code count} is a boxed {@code Long} so this record can be built directly by a
 * JPQL constructor expression, where {@code count(p)} produces a {@code Long}.
 */
public record CategoryCount(String category, Long count) {
}
