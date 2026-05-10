package com.loginapp.loginapp.DTO;

public class SettingDataDTO {

    // User Profile Data Fields 
    private String fullname;
    private String username;
    private String email;
    private String profilePictureUrl;
    private String bio;
    private String location;
    private String websiteLink;
    private String gender;
    private String profileBadge;

    // Other Data Fields
    private boolean isprivateAccount;

    // Getters and Setters
    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebsiteLink() {
        return websiteLink;
    }

    public void setWebsiteLink(String websiteLink) {
        this.websiteLink = websiteLink;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProfileBadge() {
        return profileBadge;
    }

    public void setProfileBadge(String profileBadge) {
        this.profileBadge = profileBadge;
    }

    public boolean isPrivateAccount() {
        return isprivateAccount;
    }

    public void setPrivateAccount(boolean isprivateAccount) {
        this.isprivateAccount = isprivateAccount;
    }

}