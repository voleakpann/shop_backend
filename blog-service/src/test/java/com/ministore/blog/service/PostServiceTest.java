package com.ministore.blog.service;

import com.ministore.blog.dto.PageResponse;
import com.ministore.blog.dto.PostDetail;
import com.ministore.blog.dto.PostRequest;
import com.ministore.blog.dto.PostSummary;
import com.ministore.blog.model.Post;
import com.ministore.blog.repository.PostRepository;
import com.ministore.blog.web.DuplicateSlugException;
import com.ministore.blog.web.PostNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository repository;

    @InjectMocks
    private PostService service;

    private static final String SLUG = "top-10-small-camera-in-the-world";
    private static final String CATEGORY = "Camera";
    private static final String AUTHOR_NAME = "Alice";
    private static final String AUTHOR_EMAIL = "alice@example.com";
    private static final Instant PUBLISHED_AT = Instant.parse("2023-02-22T09:00:00Z");

    // ---- list -------------------------------------------------------------

    @Test
    void listWithNoFiltersReturnsPublishedPostsNewestFirst() {
        Post post = postWith(1L, SLUG, CATEGORY, PUBLISHED_AT);
        when(repository.findByPublishedTrue(any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(List.of(post), inv.getArgument(0), 1));

        PageResponse<PostSummary> page = service.list(null, null, null, null, null);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findByPublishedTrue(captor.capture());
        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(PostService.DEFAULT_PAGE_SIZE);
        assertThat(pageable.getSort().getOrderFor("publishedAt").getDirection())
                .isEqualTo(Sort.Direction.DESC);

        assertThat(page.content()).extracting(PostSummary::slug).containsExactly(SLUG);
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.first()).isTrue();
        assertThat(page.last()).isTrue();
    }

    @Test
    void listClampsOversizedPageAndNegativePage() {
        when(repository.findByPublishedTrue(any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(List.of(), inv.getArgument(0), 0));

        service.list(null, null, null, -5, 100_000);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findByPublishedTrue(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(PostService.MAX_PAGE_SIZE);
    }

    @Test
    void listByCategoryTrimsTheFilter() {
        when(repository.findByPublishedTrueAndCategoryIgnoreCase(eq(CATEGORY), any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(List.of(), inv.getArgument(1), 0));

        service.list("  Camera  ", null, null, 0, 6);

        verify(repository).findByPublishedTrueAndCategoryIgnoreCase(eq(CATEGORY), any(Pageable.class));
    }

    @Test
    void listTreatsAllCategoryAsNoFilter() {
        when(repository.findByPublishedTrue(any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(List.of(), inv.getArgument(0), 0));

        service.list("All", null, null, 0, 6);

        verify(repository).findByPublishedTrue(any(Pageable.class));
    }

    @Test
    void listPrefersCategoryOverTagAndQuery() {
        when(repository.findByPublishedTrueAndCategoryIgnoreCase(eq(CATEGORY), any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(List.of(), inv.getArgument(1), 0));

        service.list(CATEGORY, "gear", "camera", 0, 6);

        verify(repository, never()).findPublishedByTag(any(), any());
        verify(repository, never()).findByPublishedTrueAndTitleContainingIgnoreCase(any(), any());
    }

    @Test
    void listFallsBackToTitleSearchWhenOnlyQueryIsSet() {
        when(repository.findByPublishedTrueAndTitleContainingIgnoreCase(eq("camera"), any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(List.of(), inv.getArgument(1), 0));

        service.list("  ", null, "camera", 0, 6);

        verify(repository).findByPublishedTrueAndTitleContainingIgnoreCase(eq("camera"), any(Pageable.class));
    }

    // ---- detail -----------------------------------------------------------

    @Test
    void getBySlugAssemblesNeighboursAndRelatedPosts() {
        Post post = postWith(1L, SLUG, CATEGORY, PUBLISHED_AT);
        Post older = postWith(2L, "older", CATEGORY, PUBLISHED_AT.minusSeconds(86_400));
        Post newer = postWith(3L, "newer", CATEGORY, PUBLISHED_AT.plusSeconds(86_400));
        Post related = postWith(4L, "related", CATEGORY, PUBLISHED_AT.minusSeconds(172_800));

        when(repository.findBySlugAndPublishedTrue(SLUG)).thenReturn(Optional.of(post));
        when(repository.findFirstByPublishedTrueAndPublishedAtLessThanOrderByPublishedAtDesc(PUBLISHED_AT))
                .thenReturn(Optional.of(older));
        when(repository.findFirstByPublishedTrueAndPublishedAtGreaterThanOrderByPublishedAtAsc(PUBLISHED_AT))
                .thenReturn(Optional.of(newer));
        when(repository.findTop3ByPublishedTrueAndCategoryIgnoreCaseAndIdNotOrderByPublishedAtDesc(CATEGORY, 1L))
                .thenReturn(List.of(related));

        PostDetail detail = service.getBySlug(SLUG);

        assertThat(detail.slug()).isEqualTo(SLUG);
        assertThat(detail.content()).isEqualTo("content-1");
        assertThat(detail.previous().slug()).isEqualTo("older");
        assertThat(detail.next().slug()).isEqualTo("newer");
        assertThat(detail.related()).extracting(PostSummary::slug).containsExactly("related");
    }

    @Test
    void getBySlugLeavesNeighboursNullAtTheEndsOfTheArchive() {
        Post post = postWith(1L, SLUG, CATEGORY, PUBLISHED_AT);
        when(repository.findBySlugAndPublishedTrue(SLUG)).thenReturn(Optional.of(post));
        when(repository.findFirstByPublishedTrueAndPublishedAtLessThanOrderByPublishedAtDesc(PUBLISHED_AT))
                .thenReturn(Optional.empty());
        when(repository.findFirstByPublishedTrueAndPublishedAtGreaterThanOrderByPublishedAtAsc(PUBLISHED_AT))
                .thenReturn(Optional.empty());
        when(repository.findTop3ByPublishedTrueAndCategoryIgnoreCaseAndIdNotOrderByPublishedAtDesc(CATEGORY, 1L))
                .thenReturn(List.of());

        PostDetail detail = service.getBySlug(SLUG);

        assertThat(detail.previous()).isNull();
        assertThat(detail.next()).isNull();
        assertThat(detail.related()).isEmpty();
    }

    @Test
    void getBySlugThrowsForDraftOrUnknownSlug() {
        when(repository.findBySlugAndPublishedTrue("draft")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBySlug("draft"))
                .isInstanceOf(PostNotFoundException.class);
    }

    // ---- create -----------------------------------------------------------

    @Test
    void createTakesTheAuthorFromTheTokenAndDefaultsPublishFlag() {
        PostRequest request = new PostRequest(SLUG, "Title", "Excerpt", "Body", CATEGORY,
                "/images/blog-item1.jpg", List.of("Camera"), null, null);
        when(repository.existsBySlug(SLUG)).thenReturn(false);
        when(repository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        Post result = service.create(request, AUTHOR_NAME, AUTHOR_EMAIL);

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(repository).save(captor.capture());
        Post persisted = captor.getValue();
        assertThat(persisted.getSlug()).isEqualTo(SLUG);
        assertThat(persisted.getAuthorName()).isEqualTo(AUTHOR_NAME);
        assertThat(persisted.getAuthorEmail()).isEqualTo(AUTHOR_EMAIL);
        assertThat(persisted.getTags()).containsExactly("Camera");
        assertThat(persisted.isPublished()).isTrue();
        assertThat(persisted.getPublishedAt()).isNotNull();
        assertThat(result).isSameAs(persisted);
    }

    @Test
    void createHonoursAnExplicitDraftFlagAndDate() {
        Instant backdated = Instant.parse("2022-01-01T00:00:00Z");
        PostRequest request = new PostRequest(SLUG, "Title", "Excerpt", "Body", CATEGORY,
                null, null, false, backdated);
        when(repository.existsBySlug(SLUG)).thenReturn(false);
        when(repository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        Post result = service.create(request, AUTHOR_NAME, AUTHOR_EMAIL);

        assertThat(result.isPublished()).isFalse();
        assertThat(result.getPublishedAt()).isEqualTo(backdated);
        assertThat(result.getTags()).isEmpty();
    }

    @Test
    void createRejectsADuplicateSlug() {
        PostRequest request = new PostRequest(SLUG, "Title", "Excerpt", "Body", CATEGORY,
                null, null, null, null);
        when(repository.existsBySlug(SLUG)).thenReturn(true);

        assertThatThrownBy(() -> service.create(request, AUTHOR_NAME, AUTHOR_EMAIL))
                .isInstanceOf(DuplicateSlugException.class);

        verify(repository, never()).save(any());
    }

    // ---- update / delete --------------------------------------------------

    @Test
    void updateKeepsTheOriginalAuthorAndStampsUpdatedAt() {
        Post existing = postWith(1L, SLUG, CATEGORY, PUBLISHED_AT);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        PostRequest request = new PostRequest(SLUG, "New Title", "New Excerpt", "New Body",
                "Watches", "/images/blog-item2.jpg", List.of("Watches"), null, null);
        Post result = service.update(1L, request);

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getCategory()).isEqualTo("Watches");
        assertThat(result.getTags()).containsExactly("Watches");
        assertThat(result.getAuthorEmail()).isEqualTo(AUTHOR_EMAIL);
        assertThat(result.getPublishedAt()).isEqualTo(PUBLISHED_AT);
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateRejectsASlugAlreadyUsedByAnotherPost() {
        Post existing = postWith(1L, SLUG, CATEGORY, PUBLISHED_AT);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsBySlug("taken")).thenReturn(true);

        PostRequest request = new PostRequest("taken", "Title", "Excerpt", "Body", CATEGORY,
                null, null, null, null);
        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(DuplicateSlugException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void updateAllowsKeepingTheSameSlug() {
        Post existing = postWith(1L, SLUG, CATEGORY, PUBLISHED_AT);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        PostRequest request = new PostRequest(SLUG, "Title", "Excerpt", "Body", CATEGORY,
                null, null, null, null);
        service.update(1L, request);

        verify(repository, never()).existsBySlug(any());
    }

    @Test
    void updateThrowsForAnUnknownId() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        PostRequest request = new PostRequest(SLUG, "Title", "Excerpt", "Body", CATEGORY,
                null, null, null, null);
        assertThatThrownBy(() -> service.update(99L, request))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void deleteThrowsForAnUnknownIdInsteadOfSilentlySucceeding() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(PostNotFoundException.class);

        verify(repository, never()).delete(any());
    }

    @Test
    void deleteRemovesTheFoundPost() {
        Post existing = postWith(1L, SLUG, CATEGORY, PUBLISHED_AT);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(repository).delete(existing);
    }

    private static Post postWith(Long id, String slug, String category, Instant publishedAt) {
        Post post = new Post(slug, "title-" + id, "excerpt-" + id, "content-" + id, category,
                "/images/blog-item" + id + ".jpg", AUTHOR_NAME, AUTHOR_EMAIL,
                new ArrayList<>(List.of("Camera")), true, publishedAt);
        setId(post, id);
        return post;
    }

    private static void setId(Post post, Long id) {
        try {
            var field = Post.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(post, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
