package com.ministore.comment.web;

import com.ministore.comment.dto.CommentResponse;
import com.ministore.comment.dto.CreateCommentRequest;
import com.ministore.comment.model.Comment;
import com.ministore.comment.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /** Lists a product's comments as a thread (top-level comments with nested replies). Public. */
    @GetMapping("/{slug}")
    public List<CommentResponse> list(@PathVariable String slug) {
        return commentService.listByProduct(slug);
    }

    /**
     * Posts a comment on a product, or a reply when the body's parentId is set.
     * Requires a valid JWT; the author is always taken from the token.
     */
    @PostMapping("/{slug}")
    public ResponseEntity<Comment> create(@PathVariable String slug,
                                          @Valid @RequestBody CreateCommentRequest request,
                                          @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getSubject();
        String userName = jwt.getClaimAsString("name");
        Comment saved = commentService.create(slug, request,
                userEmail, userName != null ? userName : userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
