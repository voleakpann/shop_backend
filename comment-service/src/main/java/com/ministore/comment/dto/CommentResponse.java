package com.ministore.comment.dto;

import com.ministore.comment.model.Comment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** A comment with its replies nested underneath it, for building a threaded view (YouTube-style). */
public record CommentResponse(
        Long id,
        String userName,
        String userEmail,
        String userPicture,
        String content,
        Integer likeCount,
        Instant createdAt,
        Instant editedAt,
        Boolean deleted,
        List<CommentResponse> replies) {

    public static CommentResponse of(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getUserName(),
                comment.getUserEmail(),
                comment.getUserPicture(),
                comment.getDeleted() ? "[deleted]" : comment.getContent(),
                comment.getLikeCount(),
                comment.getCreatedAt(),
                comment.getEditedAt(),
                comment.getDeleted(),
                new ArrayList<>());
    }
}
