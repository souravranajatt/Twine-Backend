package com.loginapp.loginapp.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "post_categories")
public class PostCategories {
    
    @Id
    @Column(name = "post_id")
    private Long postId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "post_id", nullable = false)
    private PostsEntity post;

    @Column(name = "primary_category", length = 50, nullable = false)
    private String primaryCategory;

    @Column(name = "sub_categories", columnDefinition = "JSON")
    private String subCategories;

    @Column(name = "topics", columnDefinition = "JSON")
    private String topics;

    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment", length = 20)
    private Sentiment sentiment;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", length = 30)
    private ContentType contentType;

    @Column(name = "confidence_score")
    private Float confidenceScore;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum Sentiment { POSITIVE, NEGATIVE, NEUTRAL }
    public enum ContentType { EDUCATIONAL, ENTERTAINMENT, NEWS, OPINION, MEME, OTHER }
    
    // Getters and Setters 
    
    public Long getPostId() {
        return postId;
    }
    public void setPostId(Long postId) {
        this.postId = postId;
    }
    public PostsEntity getPost() {
        return post;
    }
    public void setPost(PostsEntity post) {
        this.post = post;
    }
    public String getPrimaryCategory() {
        return primaryCategory;
    }
    public void setPrimaryCategory(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }
    public String getSubCategories() {
        return subCategories;
    }
    public void setSubCategories(String subCategories) {
        this.subCategories = subCategories;
    }
    public String getTopics() {
        return topics;
    }
    public void setTopics(String topics) {
        this.topics = topics;
    }
    public Sentiment getSentiment() {
        return sentiment;
    }
    public void setSentiment(Sentiment sentiment) {
        this.sentiment = sentiment;
    }
    public ContentType getContentType() {
        return contentType;
    }
    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }
    public Float getConfidenceScore() {
        return confidenceScore;
    }
    public void setConfidenceScore(Float confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    
}
