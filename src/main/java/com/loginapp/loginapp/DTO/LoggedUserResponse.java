package com.loginapp.loginapp.DTO;

public class LoggedUserResponse {
    private Long userUid;
    private String fullName;
    private String userName;
    private String profilePhoto;
    private String uBio;
    private String uLocation;
    private String uLink;
    private Long uTimeline;
    private String uGender;
    private String uBadge;

    // Getter and Setters 

    public Long getUserUid() {
        return userUid;
    }
    public void setUserUid(Long userUid) {
        this.userUid = userUid;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getProfilePhoto() {
        return profilePhoto;
    }
    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }
    public String getuBio() {
        return uBio;
    }
    public void setuBio(String uBio) {
        this.uBio = uBio;
    }
    public String getuLocation() {
        return uLocation;
    }
    public void setuLocation(String uLocation) {
        this.uLocation = uLocation;
    }
    public String getuLink() {
        return uLink;
    }
    public void setuLink(String uLink) {
        this.uLink = uLink;
    }
    public Long getuTimeline() {
        return uTimeline;
    }
    public void setuTimeline(Long uTimeline) {
        this.uTimeline = uTimeline;
    }
    public String getuGender() {
        return uGender;
    }
    public void setuGender(String uGender) {
        this.uGender = uGender;
    }
    public String getuBadge() {
        return uBadge;
    }
    public void setuBadge(String uBadge) {
        this.uBadge = uBadge;
    }

    
}
