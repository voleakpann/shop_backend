package com.ministore.comment.service;

import com.ministore.comment.dto.CommentResponse;
import com.ministore.comment.dto.CreateCommentRequest;
import com.ministore.comment.model.Comment;
import com.ministore.comment.repository.CommentRepository;
import com.ministore.comment.web.CommentNotFoundException;
import com.ministore.comment.web.MismatchedParentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository repository;

    @InjectMocks
    private CommentService service;

    private static final String PRODUCT = "widget-1";
    private static final String OTHER_PRODUCT = "widget-2";
    private static final String EMAIL = "alice@example.com";
    private static final String NAME = "Alice";
    private static final String PICTURE = "https://example.com/alice.jpg";

    private Comment saved;

    @BeforeEach
    void setUp() {
        saved = new Comment();
        saved.setProductSlug(PRODUCT);
    }

    @Test
    void createTopLevelCommentPersistsAllFields() {
        CreateCommentRequest request = new CreateCommentRequest("hello", null);
        when(repository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comment result = service.create(PRODUCT, request, EMAIL, NAME, PICTURE);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(repository).save(captor.capture());
        Comment persisted = captor.getValue();
        assertThat(persisted.getProductSlug()).isEqualTo(PRODUCT);
        assertThat(persisted.getUserEmail()).isEqualTo(EMAIL);
        assertThat(persisted.getUserName()).isEqualTo(NAME);
        assertThat(persisted.getContent()).isEqualTo("hello");
        assertThat(persisted.getParentId()).isNull();
        assertThat(persisted.getCreatedAt()).isNotNull();
        assertThat(result).isSameAs(persisted);
    }

    @Test
    void createReplyValidatesParentAndPersists() {
        Comment parent = commentWith(10L, PRODUCT, null);
        when(repository.findById(10L)).thenReturn(Optional.of(parent));
        when(repository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateCommentRequest request = new CreateCommentRequest("reply", 10L);
        Comment result = service.create(PRODUCT, request, EMAIL, NAME, PICTURE);

        assertThat(result.getParentId()).isEqualTo(10L);
        assertThat(result.getContent()).isEqualTo("reply");
    }

    @Test
    void createReplyThrowsWhenParentMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        CreateCommentRequest request = new CreateCommentRequest("orphan", 99L);
        assertThatThrownBy(() -> service.create(PRODUCT, request, EMAIL, NAME, PICTURE))
                .isInstanceOf(CommentNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void createReplyThrowsWhenParentBelongsToDifferentProduct() {
        Comment parent = commentWith(7L, OTHER_PRODUCT, null);
        when(repository.findById(7L)).thenReturn(Optional.of(parent));

        CreateCommentRequest request = new CreateCommentRequest("wrong product", 7L);
        assertThatThrownBy(() -> service.create(PRODUCT, request, EMAIL, NAME, PICTURE))
                .isInstanceOf(MismatchedParentException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void listByProductBuildsThreadedTree() {
        Comment root1 = commentWith(1L, PRODUCT, null);
        Comment root2 = commentWith(2L, PRODUCT, null);
        Comment reply1 = commentWith(3L, PRODUCT, 1L);
        Comment reply2 = commentWith(4L, PRODUCT, 1L);
        when(repository.findByProductSlugOrderByCreatedAtAsc(PRODUCT))
                .thenReturn(List.of(root1, root2, reply1, reply2));

        List<CommentResponse> roots = service.listByProduct(PRODUCT);

        assertThat(roots).extracting(CommentResponse::id).containsExactly(1L, 2L);
        CommentResponse first = roots.get(0);
        assertThat(first.replies()).extracting(CommentResponse::id).containsExactly(3L, 4L);
        assertThat(roots.get(1).replies()).isEmpty();
    }

    @Test
    void listByProductPromotesOrphanRepliesToTopLevel() {
        Comment reply = commentWith(5L, PRODUCT, 999L);
        when(repository.findByProductSlugOrderByCreatedAtAsc(PRODUCT))
                .thenReturn(List.of(reply));

        List<CommentResponse> roots = service.listByProduct(PRODUCT);

        assertThat(roots).extracting(CommentResponse::id).containsExactly(5L);
    }

    @Test
    void listByProductReturnsEmptyWhenNoComments() {
        when(repository.findByProductSlugOrderByCreatedAtAsc(PRODUCT)).thenReturn(List.of());

        assertThat(service.listByProduct(PRODUCT)).isEmpty();
    }

    private static Comment commentWith(Long id, String productSlug, Long parentId) {
        Comment c = new Comment();
        setId(c, id);
        c.setProductSlug(productSlug);
        c.setUserEmail(EMAIL);
        c.setUserName(NAME);
        c.setContent("content-" + id);
        c.setParentId(parentId);
        c.setCreatedAt(Instant.now());
        return c;
    }

    private static void setId(Comment c, Long id) {
        try {
            var field = Comment.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(c, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
