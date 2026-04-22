package com.loginapp.loginapp.entity;
import java.time.LocalDateTime;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "user_post", indexes = {
        @Index(name = "idx_post_user_id", columnList = "user_id"),
        @Index(name = "idx_post_upload_at", columnList = "upload_at")
    })
public class PostsEntity {
    
    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(
        name = "snowflake",
        strategy = "com.loginapp.loginapp.Utils.SnowflakeIdGenerator" // fixed package
    )
    @Column(name = "post_id", nullable = false, unique = true)
    private Long postId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "post_location")
    private String postLocation;

    @Column(name = "post_caption", length = 250)
    private String postCaption;

    @Column(name = "post_visiblity")
    private Boolean postVisiblity = true;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "post_tagged_users",
        joinColumns = @JoinColumn(name = "post_id")
    )
    @Column(name = "tagged_user")
    private List<String> taggedUsers;

    @Column(name = "timeline_user")
    private Long timelineUser;

    @Column(name = "like_count", columnDefinition = "BIGINT DEFAULT 0")
    private Long likeCount = 0L;

    @Column(name = "comment_count", columnDefinition = "BIGINT DEFAULT 0")
    private Long commentCount = 0L;

    @Column(name = "save_count", columnDefinition = "BIGINT DEFAULT 0")
    private Long saveCount = 0L;

    @Column(name = "view_count", columnDefinition = "BIGINT DEFAULT 0")
    private Long viewCount = 0L;

    @Column(name = "comment_enabled")
    private Boolean commentEnabled = true;

    @Column(name = "like_visible")
    private Boolean likeVisible = true;

    @Column(name = "share_enabled")
    private Boolean shareEnabled = true;

    @Column(name = "upload_at", nullable = false)
    private LocalDateTime uploadAt;
    @PrePersist
    protected void onCreate() {
            this.uploadAt = LocalDateTime.now();
    }

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users userpost;


    // Getter & Setters 
    
    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Users getUserpost() {
        return userpost;
    }

    public void setUserpost(Users userpost) {
        this.userpost = userpost;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPostCaption() {
        return postCaption;
    }

    public void setPostCaption(String postCaption) {
        this.postCaption = postCaption;
    }

    public List<String> getTaggedUsers() {
        return taggedUsers;
    }

    public void setTaggedUsers(List<String> taggedUsers) {
        this.taggedUsers = taggedUsers;
    }

    public Long getTimelineUser() {
        return timelineUser;
    }

    public void setTimelineUser(Long timelineUser) {
        this.timelineUser = timelineUser;
    }

    public LocalDateTime getUploadAt() {
        return uploadAt;
    }

    public String getPostLocation() {
        return postLocation;
    }

    public void setPostLocation(String postLocation) {
        this.postLocation = postLocation;
    }

    public Boolean getPostVisiblity() {
        return postVisiblity;
    }

    public void setPostVisiblity(Boolean postVisiblity) {
        this.postVisiblity = postVisiblity;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public Long getSaveCount() {
        return saveCount;
    }

    public void setSaveCount(Long saveCount) {
        this.saveCount = saveCount;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Boolean getCommentEnabled() {
        return commentEnabled;
    }

    public void setCommentEnabled(Boolean commentEnabled) {
        this.commentEnabled = commentEnabled;
    }

    public Boolean getLikeVisible() {
        return likeVisible;
    }

    public void setLikeVisible(Boolean likeVisible) {
        this.likeVisible = likeVisible;
    }

    public Boolean getShareEnabled() {
        return shareEnabled;
    }

    public void setShareEnabled(Boolean shareEnabled) {
        this.shareEnabled = shareEnabled;
    }
    
}
