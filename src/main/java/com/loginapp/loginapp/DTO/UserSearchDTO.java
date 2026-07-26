package com.loginapp.loginapp.DTO;

public class UserSearchDTO {
    
    private Long userId;
    private String username;
    private String fullname;
    private String profilePhoto;
    private boolean verified;
    
    public UserSearchDTO() {
    }

    public UserSearchDTO(Long userId, String username, String fullname, String profilePhoto, boolean verified) {
        this.userId = userId;
        this.username = username;
        this.fullname = fullname;
        this.profilePhoto = profilePhoto;
        this.verified = verified;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
