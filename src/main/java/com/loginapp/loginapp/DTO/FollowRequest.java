package com.loginapp.loginapp.DTO;

public class FollowRequest {
    private String userUid;
    private String actionType;

    // Getter & Setters ..

    public String getUserUid() {
        return userUid;
    }
    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }
    public String getActionType() {
        return actionType;
    }
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
        
}
