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

@Entity
@Table(
    name = "post_comment"
)
public class PostComment {

    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(
        name = "snowflake",
        strategy = "com.loginapp.loginapp.Utils.SnowflakeIdGenerator"
    )
    @Column(name = "comment_id", nullable = false, unique = true)
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostsEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = true)
    private PostComment parentId;

    @Column(name = "comment_text", nullable = false, length = 2200)
    private String commentText;

    @Column(name = "like_count")
    private Long likeCount = 0L;

    @Column(name = "reply_count")
    private Long replyCount = 0L;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Constructor 
    public PostComment(){

    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
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

    

    public PostComment getParentId() {
        return parentId;
    }

    public void setParentId(PostComment parentId) {
        this.parentId = parentId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(Long replyCount) {
        this.replyCount = replyCount;
    }

    
    
}
