package com.ministore.blog.repository;

import com.ministore.blog.dto.CategoryCount;
import com.ministore.blog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    /** Public lookup: drafts must stay invisible, so unpublished slugs read as "not found". */
    Optional<Post> findBySlugAndPublishedTrue(String slug);

    Optional<Post> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Post> findByPublishedTrue(Pageable pageable);

    Page<Post> findByPublishedTrueAndCategoryIgnoreCase(String category, Pageable pageable);

    Page<Post> findByPublishedTrueAndTitleContainingIgnoreCase(String title, Pageable pageable);

    /** Explicit count query: the join over tags would otherwise inflate the total. */
    @Query(value = "select p from Post p join p.tags t "
            + "where p.published = true and lower(t) = lower(:tag)",
            countQuery = "select count(distinct p) from Post p join p.tags t "
                    + "where p.published = true and lower(t) = lower(:tag)")
    Page<Post> findPublishedByTag(@Param("tag") String tag, Pageable pageable);

    @Query("select new com.ministore.blog.dto.CategoryCount(p.category, count(p)) "
            + "from Post p where p.published = true "
            + "group by p.category order by p.category asc")
    List<CategoryCount> countPublishedByCategory();

    /** Older neighbour, for the single-post page's "previous article" link. */
    Optional<Post> findFirstByPublishedTrueAndPublishedAtLessThanOrderByPublishedAtDesc(Instant publishedAt);

    /** Newer neighbour, for the "next article" link. */
    Optional<Post> findFirstByPublishedTrueAndPublishedAtGreaterThanOrderByPublishedAtAsc(Instant publishedAt);

    /** Same-category suggestions under an article, excluding the article itself. */
    List<Post> findTop3ByPublishedTrueAndCategoryIgnoreCaseAndIdNotOrderByPublishedAtDesc(String category, Long id);
}
