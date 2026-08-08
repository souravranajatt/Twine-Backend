package com.loginapp.loginapp.DTO;

import java.time.LocalDateTime;
import java.util.List;

public class PostFetchDTO {

    // Post Details
    private String fetchPostId;
    private String fetchFileName;
    private String fetchPostLocation;
    private String fetchPostCaption;
    private List<TaggingResult> fetchTaggedUsers;
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
    private String fullname;
    private String profileImage;
    private boolean fetchVerified;
    private boolean privateAccount;

    // Post Like and Comment 
    private Long likeCount;
    private Long commentCount;
    private Long viewCount;
    private Long saveCount;

    // Post Setting 
    private boolean commentEnable;
    private boolean shareEnable;
    private boolean likeVisible;

    // Post Flags
    private boolean likedByCurrentUser;
    private boolean savedByCurrentUser;
    private boolean ownPost;

    




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
    public List<TaggingResult> getFetchTaggedUsers() {
        return fetchTaggedUsers;
    }
    public void setFetchTaggedUsers(List<TaggingResult> fetchTaggedUsers) {
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
    public String getFullname() {
        return fullname;
    }
    public void setFullname(String fullname) {
        this.fullname = fullname;
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
    public Long getViewCount() {
        return viewCount;
    }
    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }
    public Long getSaveCount() {
        return saveCount;
    }
    public void setSaveCount(Long saveCount) {
        this.saveCount = saveCount;
    }
    public boolean isCommentEnable() {
        return commentEnable;
    }
    public void setCommentEnable(boolean commentEnable) {
        this.commentEnable = commentEnable;
    }
    public boolean isShareEnable() {
        return shareEnable;
    }
    public void setShareEnable(boolean shareEnable) {
        this.shareEnable = shareEnable;
    }
    
    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }
    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }
    public boolean isSavedByCurrentUser() {
        return savedByCurrentUser;
    }
    public void setSavedByCurrentUser(boolean savedByCurrentUser) {
        this.savedByCurrentUser = savedByCurrentUser;
    }
    public boolean isLikeVisible() {
        return likeVisible;
    }
    public void setLikeVisible(boolean likeVisible) {
        this.likeVisible = likeVisible;
    }
    public boolean isPrivateAccount() {
        return privateAccount;
    }
    public void setPrivateAccount(boolean privateAccount) {
        this.privateAccount = privateAccount;
    }
    public boolean isOwnPost() {
        return ownPost;
    }
    public void setOwnPost(boolean ownPost) {
        this.ownPost = ownPost;
    }
    
    
}
