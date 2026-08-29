package com.loginapp.loginapp.DTO;

import java.time.LocalDateTime;

public class FollowRequestListDTO {

    private String userId;
    private String username;
    private String name;
    private String profilePicture;
    private Boolean verify;
    private LocalDateTime requestedOn;

    public FollowRequestListDTO() {
    }

    // Getters and Setters

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

    // Alias for backward compatibility if needed
    public String getUserName() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Alias for fullName if needed
    public String getFullName() {
        return name;
    }

    public void setFullName(String fullName) {
        this.name = fullName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Boolean getVerify() {
        return verify;
    }

    public void setVerify(Boolean verify) {
        this.verify = verify;
    }

    public LocalDateTime getRequestedOn() {
        return requestedOn;
    }

    public void setRequestedOn(LocalDateTime requestedOn) {
        this.requestedOn = requestedOn;
    }

    public LocalDateTime getRequestDate() {
        return requestedOn;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestedOn = requestDate;
    }
}
