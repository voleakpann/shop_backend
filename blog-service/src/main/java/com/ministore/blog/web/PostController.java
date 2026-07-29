package com.ministore.blog.web;

import com.ministore.blog.dto.CategoryCount;
import com.ministore.blog.dto.PageResponse;
import com.ministore.blog.dto.PostDetail;
import com.ministore.blog.dto.PostRequest;
import com.ministore.blog.dto.PostSummary;
import com.ministore.blog.model.Post;
import com.ministore.blog.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * Public: one page of published posts, newest first — this backs the blog list page.
     * Optional filters: {@code category}, {@code tag}, or {@code q} (title search).
     */
    @GetMapping
    public PageResponse<PostSummary> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return postService.list(category, tag, q, page, size);
    }

    /**
     * Public: category names with post counts, for the blog sidebar.
     * Mapped before {@code /{slug}} — "categories" is therefore a reserved slug.
     */
    @GetMapping("/categories")
    public List<CategoryCount> categories() {
        return postService.categories();
    }

    /**
     * Public: one article plus its previous/next links and related posts, so the
     * single-post page renders from a single request. 404 for drafts and unknown slugs.
     */
    @GetMapping("/{slug}")
    public PostDetail bySlug(@PathVariable String slug) {
        return postService.getBySlug(slug);
    }

    /** Admin-only: create a post. The byline always comes from the JWT. */
    @PostMapping
    public ResponseEntity<PostDetail> create(@Valid @RequestBody PostRequest request,
                                             @AuthenticationPrincipal Jwt jwt) {
        String authorEmail = jwt.getSubject();
        String authorName = jwt.getClaimAsString("name");
        Post saved = postService.create(request, authorName != null ? authorName : authorEmail, authorEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PostDetail.of(saved, null, null, List.of()));
    }

    /** Admin-only: replace a post's editable fields. */
    @PutMapping("/{id}")
    public PostDetail update(@PathVariable Long id, @Valid @RequestBody PostRequest request) {
        Post saved = postService.update(id, request);
        return PostDetail.of(saved, null, null, List.of());
    }

    /** Admin-only: delete a post. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
