package com.loginapp.loginapp.DTO;

import java.time.LocalDateTime;
import java.util.List;

public class PostFetchDTO {
    private String fetchPostId;
    private String fetchFileName;
    private String fetchPostLocation;
    private String fetchPostCaption;
    private List<String> fetchTaggedUsers;
    private String fetchTimelineUser;
    private LocalDateTime fetchUploadAt;
    private boolean fetchVerified;    


    // Getter and Setter 
    
    public String getFetchPostId() {
        return fetchPostId;
    }
    public void setFetchPostId(String fetchPostId) {
        this.fetchPostId = fetchPostId;
    }
    public String getFetchFileName() {
        return fetchFileName;
    }
    public void setFetchFileName(String fetchFileName) {
        this.fetchFileName = fetchFileName;
    }
    public String getFetchPostLocation() {
        return fetchPostLocation;
    }
    public void setFetchPostLocation(String fetchPostLocation) {
        this.fetchPostLocation = fetchPostLocation;
    }
    public String getFetchPostCaption() {
        return fetchPostCaption;
    }
    public void setFetchPostCaption(String fetchPostCaption) {
        this.fetchPostCaption = fetchPostCaption;
    }
    public List<String> getFetchTaggedUsers() {
        return fetchTaggedUsers;
    }
    public void setFetchTaggedUsers(List<String> fetchTaggedUsers) {
        this.fetchTaggedUsers = fetchTaggedUsers;
    }
    public String getFetchTimelineUser() {
        return fetchTimelineUser;
    }
    public void setFetchTimelineUser(String fetchTimelineUser) {
        this.fetchTimelineUser = fetchTimelineUser;
    }
    public LocalDateTime getFetchUploadAt() {
        return fetchUploadAt;
    }
    public void setFetchUploadAt(LocalDateTime fetchUploadAt) {
        this.fetchUploadAt = fetchUploadAt;
    }
    public boolean isFetchVerified() {
        return fetchVerified;
    }
    public void setFetchVerified(boolean fetchVerified) {
        this.fetchVerified = fetchVerified;
    }
    
}
