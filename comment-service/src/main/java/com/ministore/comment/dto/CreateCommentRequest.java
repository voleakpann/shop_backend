package com.ministore.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body for posting a comment or a reply. There is no {@code userEmail} or
 * {@code userName} here — identity always comes from the JWT, never the client.
 * When {@code parentId} is null this is a top-level comment; otherwise it's a
 * reply to that comment.
 */
public record CreateCommentRequest(
        @NotBlank @Size(max = 2000) String content,
        Long parentId) {
}
