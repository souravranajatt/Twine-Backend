package com.loginapp.loginapp.DTO;

public class SettingIntreactionDTO {
    private boolean commentingEnable;
    private boolean likeVisible;
    private String taggingEnable;
    private String mentionEnable;
    private boolean discoverable;

    public SettingIntreactionDTO() {
    }

    public boolean isCommentingEnable() {
        return commentingEnable;
    }

    public void setCommentingEnable(boolean commentingEnable) {
        this.commentingEnable = commentingEnable;
    }

    public boolean isLikeVisible() {
        return likeVisible;
    }

    public void setLikeVisible(boolean likeVisible) {
        this.likeVisible = likeVisible;
    }

    public String getTaggingEnable() {
        return taggingEnable;
    }

    public void setTaggingEnable(String taggingEnable) {
        this.taggingEnable = taggingEnable;
    }

    public String getMentionEnable() {
        return mentionEnable;
    }

    public void setMentionEnable(String mentionEnable) {
        this.mentionEnable = mentionEnable;
    }

    public boolean isDiscoverable() {
        return discoverable;
    }

    public void setDiscoverable(boolean discoverable) {
        this.discoverable = discoverable;
    }
}
