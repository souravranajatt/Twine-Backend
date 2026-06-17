package com.loginapp.loginapp.DTO;

import java.time.LocalDateTime;

public class PostCommentFetchDTO {

    // 1. Comment Details
    private Long commentId;
    private String commentText;
    private LocalDateTime createdAt;
    
    // 2.  User Details
    private String userId;
    private String username;
    private String profileImage;
    private boolean fetchVerified;
    
    // 3. Threading & Action Details (For nested replies and likes)
    private Long parentId;      
    private Long replyCount;     
    private Long likeCount;      
    private boolean likedByCurrentUser; 


    // Getters and Setters ...


    public Long getCommentId() {
        return commentId;
    }
    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }
    public String getCommentText() {
        return commentText;
    }
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getProfileImage() {
        return profileImage;
    }
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
    public boolean isFetchVerified() {
        return fetchVerified;
    }
    public void setFetchVerified(boolean fetchVerified) {
        this.fetchVerified = fetchVerified;
    }
    public Long getParentId() {
        return parentId;
    }
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    public Long getReplyCount() {
        return replyCount;
    }
    public void setReplyCount(Long replyCount) {
        this.replyCount = replyCount;
    }
    public Long getLikeCount() {
        return likeCount;
    }
    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }
    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }
    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }    
    
}

