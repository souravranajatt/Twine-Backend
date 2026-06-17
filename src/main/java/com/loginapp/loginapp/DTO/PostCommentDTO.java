package com.loginapp.loginapp.DTO;

public class PostCommentDTO {
    
    private Long parentId;
    private String commentText;

    public Long getParentId() {
        return parentId;
    }
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    public String getCommentText() {
        return commentText;
    }
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    } 

    
}
