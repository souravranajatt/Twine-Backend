package com.loginapp.loginapp.DTO;

import java.time.LocalDateTime;
import java.util.List;

public class SearchUserResponse {

    // ---------- User ----------
    private String searchUserId;
    private String searchUsername;
    private String searchFullname;
    private boolean searchVerified;
    private LocalDateTime searchCreatedAt;

    // ---------- User Data ----------
    private String searchProfilePhoto;
    private String searchUserBio;
    private String searchUserLocation;
    private String searchUserLink;
    private String searchUserTimeline;
    private String searchBadge;
    private String searchUserGender;

    // ---------- Private / Public Flag ---------
    private boolean searchPrivate;
    private boolean searchPrivateShow;

    // ---------- Meta Flags ----------
    private boolean searchLoggedUser;
    private boolean followingStatus;
    private boolean followerStatus;
    private boolean followReqStatus;
    private boolean followReqOptStatus;

    // ---------- Counts ----------
    private Long followersCount;
    private Long followingCount;
    private Long postCount;

    // Getter && Setter 

    public String getSearchUserId() {
        return searchUserId;
    }

    public void setSearchUserId(String searchUserId) {
        this.searchUserId = searchUserId;
    }

    public String getSearchUsername() {
        return searchUsername;
    }

    public void setSearchUsername(String searchUsername) {
        this.searchUsername = searchUsername;
    }

    public String getSearchFullname() {
        return searchFullname;
    }

    public void setSearchFullname(String searchFullname) {
        this.searchFullname = searchFullname;
    }

    public boolean isSearchVerified() {
        return searchVerified;
    }

    public void setSearchVerified(boolean searchVerified) {
        this.searchVerified = searchVerified;
    }

    public boolean isSearchPrivate() {
        return searchPrivate;
    }

    public void setSearchPrivate(boolean searchPrivate) {
        this.searchPrivate = searchPrivate;
    }

    public boolean isSearchPrivateShow() {
        return searchPrivateShow;
    }

    public void setSearchPrivateShow(boolean searchPrivateShow) {
        this.searchPrivateShow = searchPrivateShow;
    }

    public LocalDateTime getSearchCreatedAt() {
        return searchCreatedAt;
    }

    public void setSearchCreatedAt(LocalDateTime searchCreatedAt) {
        this.searchCreatedAt = searchCreatedAt;
    }

    public String getSearchProfilePhoto() {
        return searchProfilePhoto;
    }

    public void setSearchProfilePhoto(String searchProfilePhoto) {
        this.searchProfilePhoto = searchProfilePhoto;
    }

    public String getSearchUserBio() {
        return searchUserBio;
    }

    public void setSearchUserBio(String searchUserBio) {
        this.searchUserBio = searchUserBio;
    }

    public String getSearchUserLocation() {
        return searchUserLocation;
    }

    public void setSearchUserLocation(String searchUserLocation) {
        this.searchUserLocation = searchUserLocation;
    }

    public String getSearchUserLink() {
        return searchUserLink;
    }

    public void setSearchUserLink(String searchUserLink) {
        this.searchUserLink = searchUserLink;
    }

    public String getSearchUserTimeline() {
        return searchUserTimeline;
    }

    public void setSearchUserTimeline(String searchUserTimeline) {
        this.searchUserTimeline = searchUserTimeline;
    }

    public String getSearchBadge() {
        return searchBadge;
    }

    public void setSearchBadge(String searchBadge) {
        this.searchBadge = searchBadge;
    }

    public String getSearchUserGender() {
        return searchUserGender;
    }

    public void setSearchUserGender(String searchUserGender) {
        this.searchUserGender = searchUserGender;
    }

    public boolean isSearchLoggedUser() {
        return searchLoggedUser;
    }

    public void setSearchLoggedUser(boolean searchLoggedUser) {
        this.searchLoggedUser = searchLoggedUser;
    }

    public boolean isFollowingStatus() {
        return followingStatus;
    }

    public void setFollowingStatus(boolean followingStatus) {
        this.followingStatus = followingStatus;
    }

    public boolean isFollowReqStatus() {
        return followReqStatus;
    }

    public void setFollowReqStatus(boolean followReqStatus) {
        this.followReqStatus = followReqStatus;
    }

    public boolean isFollowReqOptStatus() {
        return followReqOptStatus;
    }

    public void setFollowReqOptStatus(boolean followReqOptStatus) {
        this.followReqOptStatus = followReqOptStatus;
    }

    public Long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Long followersCount) {
        this.followersCount = followersCount;
    }

    public Long getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(Long followingCount) {
        this.followingCount = followingCount;
    }

    public Long getPostCount() {
        return postCount;
    }

    public void setPostCount(Long postCount) {
        this.postCount = postCount;
    }

    public boolean isFollowerStatus() {
        return followerStatus;
    }
    
    public void setFollowerStatus(boolean followerStatus) {
        this.followerStatus = followerStatus;
    }
    

}
