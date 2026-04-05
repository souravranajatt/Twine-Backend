package com.loginapp.loginapp.entity;
import java.time.LocalDateTime;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "user_post")
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

    @Column(name = "user_location")
    private String userLocation;

    @Column(name = "post_caption", length = 250)
    private String postCaption;

    @Column(name = "post_visiblity")
    private Boolean postVisiblity = true;
    
    @Column(name = "tagged_user")
    @ElementCollection
    private List<String> taggedUsers;

    @Column(name = "timeline_user")
    private Long timelineUser;

    @Column(name = "upload_at", nullable = false, updatable = false)
    private LocalDateTime uploadAt;
    @PrePersist
    protected void onCreate() {
        if (this.uploadAt == null) {
            this.uploadAt = LocalDateTime.now();
        }
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

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public Boolean getPostVisiblity() {
        return postVisiblity;
    }

    public void setPostVisiblity(Boolean postVisiblity) {
        this.postVisiblity = postVisiblity;
    }
    
}
