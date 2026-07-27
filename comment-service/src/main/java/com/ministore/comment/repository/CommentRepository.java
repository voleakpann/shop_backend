package com.ministore.comment.repository;

import com.ministore.comment.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** All comments and replies for a product, oldest first so replies build the tree correctly. */
    List<Comment> findByProductSlugOrderByCreatedAtAsc(String productSlug);
}
