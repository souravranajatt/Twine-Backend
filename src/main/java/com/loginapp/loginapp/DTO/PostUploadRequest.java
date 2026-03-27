package com.loginapp.loginapp.DTO;

import org.springframework.web.multipart.MultipartFile;

public class PostUploadRequest {

    private String postCaption;
    private int postTimelineUser;
    private MultipartFile file;
    private String photoLocation;

    // Getter and Setter

    public String getPostCaption() {
        return postCaption;
    }
    public void setPostCaption(String postCaption) {
        this.postCaption = postCaption;
    }
    public MultipartFile getFile() {
        return file;
    }
    public void setFile(MultipartFile file) {
        this.file = file;
    }
    public int getPostTimelineUser() {
        return postTimelineUser;
    }
    public void setPostTimelineUser(int postTimelineUser) {
        this.postTimelineUser = postTimelineUser;
    }
    public String getPhotoLocation() {
        return photoLocation;
    }
    public void setPhotoLocation(String photoLocation) {
        this.photoLocation = photoLocation;
    }
    
}
