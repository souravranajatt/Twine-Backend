package com.loginapp.loginapp.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "saved_posts", 
    uniqueConstraints= @UniqueConstraint(columnNames = {"user_id", "post_id"})
)
public class SavedPosts {

    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(
        name = "snowflake",
        strategy = "com.loginapp.loginapp.Utils.SnowflakeIdGenerator"
    )
    @Column(name = "saved_id", nullable = false, unique = true)
    private Long savedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostsEntity post;

    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;

    public SavedPosts() {
    }

    @PrePersist
    protected void onCreate() {
        if (savedAt == null) {
            savedAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        }
    }

    // Getters and Setters 

    public Long getSavedId() {
        return savedId;
    }
    public void setSavedId(Long savedId) {
        this.savedId = savedId;
    }
    public Users getUser() {
        return user;
    }
    public void setUser(Users user) {
        this.user = user;
    }
    public PostsEntity getPost() {
        return post;
    }
    public void setPost(PostsEntity post) {
        this.post = post;
    }
    public LocalDateTime getSavedAt() {
        return savedAt;
    }

}
