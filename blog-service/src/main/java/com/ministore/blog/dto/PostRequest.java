package com.ministore.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

/**
 * Body for creating or updating a post. There is no author field — identity always
 * comes from the JWT, never the client. {@code publishedAt} is optional; when null
 * the service stamps "now" on create and leaves the existing date on update.
 */
public record PostRequest(
        @NotBlank
        @Size(max = 200)
        @Pattern(regexp = "[a-z0-9]+(-[a-z0-9]+)*",
                message = "must be lowercase words separated by single hyphens")
        String slug,

        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 500) String excerpt,
        @NotBlank String content,
        @NotBlank @Size(max = 100) String category,
        @Size(max = 500) String coverImage,
        List<String> tags,
        Boolean published,
        Instant publishedAt) {
}
