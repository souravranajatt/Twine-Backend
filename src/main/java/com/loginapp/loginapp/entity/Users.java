package com.loginapp.loginapp.entity;

import java.time.LocalDateTime;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "signup")
public class Users {

    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(
        name = "snowflake",
        strategy = "com.loginapp.loginapp.Utils.SnowflakeIdGenerator" // fixed package
    )
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "fullname", length = 30, nullable = false)
    private String fullname;

    @Column(name = "username", length = 25, nullable = false, unique = true)
    private String username;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", length = 100, nullable = false)
    private String passwordHash;

    @Column(name = "verify_tag", updatable = true)
    private boolean verifyTag = false;

    @Column(name = "is_deleted", updatable = true)
    private boolean statusDeleted = false;

    @Column(name = "is_private", updatable = true)
    private boolean statusPrivate = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // UserData Mapping using user_id for attach Information
    @OneToOne(mappedBy = "users", cascade = CascadeType.ALL)
    private UserData userData;

    // PostEntity attach using user_id for accessing Post for User 
    @OneToMany(mappedBy = "userpost", cascade = CascadeType.ALL)
    private List<PostsEntity> postsEntity;

    











    // Getters & Setters
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public UserData getUserData() {
        return userData;
    }
    public void setUserData(UserData userData) {
        this.userData = userData;
    }
    public List<PostsEntity> getPostsEntity() {
        return postsEntity;
    }
    public void setPostsEntity(List<PostsEntity> postsEntity) {
        this.postsEntity = postsEntity;
    }
    public boolean isVerifyTag() {
        return verifyTag;
    }
    public void setVerifyTag(boolean verifyTag) {
        this.verifyTag = verifyTag;
    }
    public boolean isStatusDeleted() {
        return statusDeleted;
    }
    public void setStatusDeleted(boolean statusDeleted) {
        this.statusDeleted = statusDeleted;
    }
    public boolean isStatusPrivate() {
        return statusPrivate;
    }
    public void setStatusPrivate(boolean statusPrivate) {
        this.statusPrivate = statusPrivate;
    }
    
}