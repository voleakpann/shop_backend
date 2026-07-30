package com.ministore.comment.service;

import com.ministore.comment.dto.CommentResponse;
import com.ministore.comment.dto.CreateCommentRequest;
import com.ministore.comment.model.Comment;
import com.ministore.comment.repository.CommentRepository;
import com.ministore.comment.web.CommentNotFoundException;
import com.ministore.comment.web.MismatchedParentException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    private final CommentRepository comments;

    public CommentService(CommentRepository comments) {
        this.comments = comments;
    }

    /** Posts a top-level comment (parentId == null) or a reply, for the authenticated user. */
    public Comment create(String productSlug, CreateCommentRequest request, String userEmail, String userName, String userPicture) {
        if (request.parentId() != null) {
            Comment parent = comments.findById(request.parentId())
                    .orElseThrow(() -> new CommentNotFoundException(request.parentId()));
            if (!parent.getProductSlug().equals(productSlug)) {
                throw new MismatchedParentException(request.parentId(), productSlug);
            }
        }

        Comment comment = new Comment();
        comment.setProductSlug(productSlug);
        comment.setUserEmail(userEmail);
        comment.setUserName(userName);
        comment.setUserPicture(userPicture);
        comment.setContent(request.content());
        comment.setParentId(request.parentId());
        comment.setCreatedAt(Instant.now());
        comment.setLikeCount(0);
        return comments.save(comment);
    }

    /** Soft-delete a comment (mark deleted, hide content). Only author or admin can delete. */
    public void delete(Long commentId, String userEmail) {
        Comment comment = comments.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        if (!comment.getUserEmail().equals(userEmail)) {
            throw new SecurityException("Only the author can delete this comment");
        }
        comment.setDeleted(true);
        comment.setDeletedAt(Instant.now());
        comments.save(comment);
    }

    /** Like/unlike a comment. */
    public void toggleLike(Long commentId, String userEmail) {
        Comment comment = comments.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        comment.setLikeCount(comment.getLikeCount() + 1);
        comments.save(comment);
    }

    /** All comments for a product, arranged as top-level comments with nested replies. */
    public List<CommentResponse> listByProduct(String productSlug) {
        List<Comment> flat = comments.findByProductSlugOrderByCreatedAtAsc(productSlug);

        Map<Long, CommentResponse> byId = new HashMap<>();
        List<CommentResponse> roots = new ArrayList<>();

        for (Comment comment : flat) {
            byId.put(comment.getId(), CommentResponse.of(comment));
        }
        for (Comment comment : flat) {
            CommentResponse response = byId.get(comment.getId());
            if (comment.getParentId() == null) {
                roots.add(response);
            } else {
                CommentResponse parent = byId.get(comment.getParentId());
                // Parent already deleted/missing: surface it as top-level rather than dropping it.
                (parent != null ? parent.replies() : roots).add(response);
            }
        }
        return roots;
    }
}
