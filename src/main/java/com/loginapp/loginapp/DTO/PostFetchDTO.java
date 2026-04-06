package com.loginapp.loginapp.DTO;

import java.time.LocalDateTime;
import java.util.List;

public class PostFetchDTO {
    private String fetchPostId;
    private String fetchFileName;
    private String fetchPostLocation;
    private String fetchPostCaption;
    private List<String> fetchTaggedUsers;
    private String fetchTimelineUser;
    private LocalDateTime fetchUploadAt;

    // Post Metadata
    private Integer width;
    private Integer height;
    private Integer duration;
    private String postType;

    // Post User Details
    private String userId;
    private String username;
    private String profileImage;
    private boolean fetchVerified;


    // Getter and Setter 
    
    public String getFetchPostId() {
        return fetchPostId;
    }
    public void setFetchPostId(String fetchPostId) {
        this.fetchPostId = fetchPostId;
    }
    public String getFetchFileName() {
        return fetchFileName;
    }
    public void setFetchFileName(String fetchFileName) {
        this.fetchFileName = fetchFileName;
    }
    public String getFetchPostLocation() {
        return fetchPostLocation;
    }
    public void setFetchPostLocation(String fetchPostLocation) {
        this.fetchPostLocation = fetchPostLocation;
    }
    public String getFetchPostCaption() {
        return fetchPostCaption;
    }
    public void setFetchPostCaption(String fetchPostCaption) {
        this.fetchPostCaption = fetchPostCaption;
    }
    public List<String> getFetchTaggedUsers() {
        return fetchTaggedUsers;
    }
    public void setFetchTaggedUsers(List<String> fetchTaggedUsers) {
        this.fetchTaggedUsers = fetchTaggedUsers;
    }
    public String getFetchTimelineUser() {
        return fetchTimelineUser;
    }
    public void setFetchTimelineUser(String fetchTimelineUser) {
        this.fetchTimelineUser = fetchTimelineUser;
    }
    public LocalDateTime getFetchUploadAt() {
        return fetchUploadAt;
    }
    public void setFetchUploadAt(LocalDateTime fetchUploadAt) {
        this.fetchUploadAt = fetchUploadAt;
    }
    public boolean isFetchVerified() {
        return fetchVerified;
    }
    public void setFetchVerified(boolean fetchVerified) {
        this.fetchVerified = fetchVerified;
    }
    public Integer getWidth() {
        return width;
    }
    public void setWidth(Integer width) {
        this.width = width;
    }
    public Integer getHeight() {
        return height;
    }
    public void setHeight(Integer height) {
        this.height = height;
    }
    public Integer getDuration() {
        return duration;
    }
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    public String getPostType() {
        return postType;
    }
    public void setPostType(String postType) {
        this.postType = postType;
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
    
}
