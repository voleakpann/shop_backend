package com.ministore.comment.dto;

import com.ministore.comment.model.Comment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** A comment with its replies nested underneath it, for building a threaded view. */
public record CommentResponse(
        Long id,
        String userName,
        String content,
        Instant createdAt,
        List<CommentResponse> replies) {

    public static CommentResponse of(Comment comment) {
        return new CommentResponse(comment.getId(), comment.getUserName(), comment.getContent(),
                comment.getCreatedAt(), new ArrayList<>());
    }
}
