package com.ministore.blog.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** A blog article, as rendered by the frontend's blog list and single-post pages. */
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** URL segment used by the single-post page, e.g. "top-10-small-camera-in-the-world". */
    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    /** Short teaser shown under the title in the blog list. */
    @Column(nullable = false, length = 500)
    private String excerpt;

    /** Full article body (markdown or HTML — the frontend decides how to render it). */
    @Column(nullable = false, columnDefinition = "text")
    private String content;

    /** Single category shown next to the date, e.g. "Camera". */
    @Column(nullable = false)
    private String category;

    private String coverImage;

    /** Display name of the author, denormalised so the blog needs no call to auth-service. */
    @Column(nullable = false)
    private String authorName;

    /** Email of the admin who created the post (taken from the JWT, never the client). */
    @Column(nullable = false)
    private String authorEmail;

    /**
     * LAZY on purpose: an EAGER collection forces Hibernate to paginate in memory,
     * which breaks the paged blog list. The service maps posts to DTOs inside a
     * transaction, so the tags are always initialised before serialisation.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    /** Drafts (false) are hidden from every public endpoint. */
    @Column(nullable = false)
    private boolean published = true;

    /** Drives the "FEB 22, 2023" line and the newest-first ordering. */
    @Column(nullable = false)
    private Instant publishedAt = Instant.now();

    private Instant updatedAt;

    public Post() {
    }

    public Post(String slug, String title, String excerpt, String content, String category,
                String coverImage, String authorName, String authorEmail, List<String> tags,
                boolean published, Instant publishedAt) {
        this.slug = slug;
        this.title = title;
        this.excerpt = excerpt;
        this.content = content;
        this.category = category;
        this.coverImage = coverImage;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.tags = tags;
        this.published = published;
        this.publishedAt = publishedAt;
    }

    public Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
