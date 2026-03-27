package com.loginapp.loginapp.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "follow_data",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"follower_uid", "following_uid"})
    }
)
public class FollowUser {

    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(
        name = "snowflake",
        strategy = "com.loginapp.loginapp.Utils.SnowflakeIdGenerator" // fixed package
    )
    @Column(name = "follow_id", nullable = false, unique = true)
    private Long followId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_uid", nullable = false)
    private Users following;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_uid", nullable = false)
    private Users follower;

    @Column(name = "create_at", nullable = false, updatable = false)
    private LocalDateTime followedOn;

    @PrePersist
    protected void onCreate() {
        if (followedOn == null) {
            followedOn = LocalDateTime.now();
        }
    }

    // Getters and Setters 

    public Long getFollowId() {
        return followId;
    }

    public void setFollowId(Long followId) {
        this.followId = followId;
    }

    public Users getFollowing() {
        return following;
    }

    public void setFollowing(Users following) {
        this.following = following;
    }

    public Users getFollower() {
        return follower;
    }

    public void setFollower(Users follower) {
        this.follower = follower;
    }

    public LocalDateTime getFollowedOn() {
        return followedOn;
    }
    
}
