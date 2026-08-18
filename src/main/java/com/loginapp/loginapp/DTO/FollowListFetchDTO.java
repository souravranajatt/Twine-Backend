package com.loginapp.loginapp.DTO;

public class FollowListFetchDTO {

    private String username;
    private String name;
    private String userId;
    private String profilePicture;
    private Boolean followsYou;
    private Boolean followedByMe;
    private Boolean verify;
    private Boolean isMe;

    // Getters & Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public Boolean getFollowsYou() { return followsYou; }
    public void setFollowsYou(Boolean followsYou) { this.followsYou = followsYou; }

    public Boolean getFollowedByMe() { return followedByMe; }
    public void setFollowedByMe(Boolean followedByMe) { this.followedByMe = followedByMe; }

    public Boolean getVerify() { return verify; }
    public void setVerify(Boolean verify) { this.verify = verify; }

    public Boolean getIsMe() { return isMe; }
    public void setIsMe(Boolean isMe) { this.isMe = isMe; }
}