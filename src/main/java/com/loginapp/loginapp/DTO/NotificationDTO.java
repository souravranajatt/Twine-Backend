package com.loginapp.loginapp.DTO;

public class NotificationDTO {

    private String notificationId;
    private String type;
    private String actorId;
    private String actorUsername;
    private String actorFullname;
    private String actorProfilePhoto;
    private boolean actorVerified;
    private String targetId;
    private String previewText;
    private int aggregateCount;
    private String message;
    private boolean read;
    private String createdAt;

    public NotificationDTO() {}

    // Getters and Setters

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public String getActorFullname() {
        return actorFullname;
    }

    public void setActorFullname(String actorFullname) {
        this.actorFullname = actorFullname;
    }

    public String getActorProfilePhoto() {
        return actorProfilePhoto;
    }

    public void setActorProfilePhoto(String actorProfilePhoto) {
        this.actorProfilePhoto = actorProfilePhoto;
    }

    public boolean isActorVerified() {
        return actorVerified;
    }

    public void setActorVerified(boolean actorVerified) {
        this.actorVerified = actorVerified;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getPreviewText() {
        return previewText;
    }

    public void setPreviewText(String previewText) {
        this.previewText = previewText;
    }

    public int getAggregateCount() {
        return aggregateCount;
    }

    public void setAggregateCount(int aggregateCount) {
        this.aggregateCount = aggregateCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
