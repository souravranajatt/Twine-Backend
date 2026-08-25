package com.loginapp.loginapp.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "viewed_user",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "search_user_id"})
    },
    indexes = {
        @Index(name = "idx_viewed_user_id", columnList = "user_id"),
        @Index(name = "idx_viewed_search_user_id", columnList = "search_user_id")
    }
)
public class ViewedUser {

    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(
        name = "snowflake",
        strategy = "com.loginapp.loginapp.Utils.SnowflakeIdGenerator" // fixed package
    )
    @Column(name = "viewed_id", nullable = false, unique = true)
    private Long viewedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "search_user_id", nullable = false)
    private Users searchUser;

    @Column(name = "search_count", nullable = false, columnDefinition = "int default 0")
    private int searchCount = 0;

    @Column(name = "last_searched_at", nullable = true)
    private LocalDateTime lastSearchedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ViewedUser() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        }
        if (this.lastSearchedAt == null) {
            this.lastSearchedAt = this.createdAt;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastSearchedAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    }

    public Long getViewedId() {
        return viewedId;
    }

    public void setViewedId(Long viewedId) {
        this.viewedId = viewedId;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Users getSearchUser() {
        return searchUser;
    }

    public void setSearchUser(Users searchUser) {
        this.searchUser = searchUser;
    }

    public int getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(int searchCount) {
        this.searchCount = searchCount;
    }

    public LocalDateTime getLastSearchedAt() {
        return lastSearchedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    

}
