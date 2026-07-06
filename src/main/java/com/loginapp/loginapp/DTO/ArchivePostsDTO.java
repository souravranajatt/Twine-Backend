package com.loginapp.loginapp.DTO;

import java.time.LocalDateTime;

public class ArchivePostsDTO {
    private String postId;
    private String postContent;
    private String postCaption;
    private LocalDateTime uploadAt;
    private String postType;

    
    public String getPostId() {
        return postId;
    }
    public void setPostId(String postId) {
        this.postId = postId;
    }
    public String getPostContent() {
        return postContent;
    }
    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }
    public String getPostCaption() {
        return postCaption;
    }
    public void setPostCaption(String postCaption) {
        this.postCaption = postCaption;
    }
    public LocalDateTime getUploadAt() {
        return uploadAt;
    }
    public void setUploadAt(LocalDateTime uploadAt) {
        this.uploadAt = uploadAt;
    }
    public String getPostType() {
        return postType;
    }
    public void setPostType(String postType) {
        this.postType = postType;
    }

    
}
