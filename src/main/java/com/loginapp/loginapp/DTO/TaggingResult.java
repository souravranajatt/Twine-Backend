package com.loginapp.loginapp.DTO;

public class TaggingResult {
    private String userId;
    private String username;
    private String profileImage;
    private boolean allowTagging;
    private boolean verify;

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

    public boolean isAllowTagging() {
        return allowTagging;
    }

    public void setAllowTagging(boolean allowTagging) {
        this.allowTagging = allowTagging;
    }

    public boolean isVerify() {
        return verify;
    }
    public void setVerify(boolean verify) {
        this.verify = verify;
    }
    
}
