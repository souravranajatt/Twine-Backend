package com.loginapp.loginapp.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name="user_data")
public class UserData {
    
    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;

    @Column(name = "profile_photo")
    private String profilePhoto;

    @Column(name = "user_bio",length = 101)
    private String userBio;

    @Column(name = "user_location")
    private String userLocation;

    @Column(name = "user_link")
    private String userlink;

    @Column(name = "timeline_user")
    private Long timeUser;

    @Column(name = "user_gender")
    private String userGender;

    @Column(name = "user_badge")
    private String badge;

    @Column(name = "update_at", nullable = false, updatable = true)
    private LocalDateTime updateAt;
    @PrePersist
    protected void onCreate() {
            this.updateAt = LocalDateTime.now();
    }

    // Getters and Setters 

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getProfilePhoto() {
        return profilePhoto;
    }
    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }
    public String getUserBio() {
        return userBio;
    }
    public void setUserBio(String userBio) {
        this.userBio = userBio;
    }
    public String getUserLocation() {
        return userLocation;
    }
    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }
    public String getUserlink() {
        return userlink;
    }
    public void setUserlink(String userlink) {
        this.userlink = userlink;
    }
    public Long getTimeUser() {
        return timeUser;
    }
    public void setTimeUser(Long timeUser) {
        this.timeUser = timeUser;
    }
    public String getBadge() {
        return badge;
    }
    public void setBadge(String badge) {
        this.badge = badge;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt; 
    }
    public Users getUsers() {
        return users;
    }
    public void setUsers(Users users) {
        this.users = users;
    }
    public String getUserGender() {
        return userGender;
    }
    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }
    
}
