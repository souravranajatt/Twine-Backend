package com.loginapp.loginapp.DTO;

public class PostCommentDTO {
    
    private String parentId;
    private String commentText;

    public String getParentId() {
        return parentId;
    }
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    public String getCommentText() {
        return commentText;
    }
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    } 

    
}
