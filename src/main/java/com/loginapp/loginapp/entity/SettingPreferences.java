package com.loginapp.loginapp.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.hibernate.annotations.GenericGenerator;
import jakarta.persistence.*;

@Entity
@Table(name = "setting_preferences")
public class SettingPreferences {

    public enum PreferenceVisibility {
        EVERYONE,
        FOLLOWERS_ONLY,
        NO_ONE
    }

    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(
        name = "snowflake",
        strategy = "com.loginapp.loginapp.Utils.SnowflakeIdGenerator"
    )
    @Column(name = "setting_id", nullable = false, unique = true)
    private Long settingId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Users user;

    @Column(name = "commenting", nullable = false)
    private boolean commentingEnable = true;

    @Column(name = "like_visible", nullable = false)
    private boolean likeVisible = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag", nullable = false)
    private PreferenceVisibility taggingEnable = PreferenceVisibility.EVERYONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "mention", nullable = false)
    private PreferenceVisibility mentionEnable = PreferenceVisibility.EVERYONE;

    @Column(name = "discoverable", nullable = false)
    private boolean discoverable = true;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", updatable = true)
    private LocalDateTime updatedAt;

    public SettingPreferences() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    }

    // ---------- Getters & Setters ----------

    public Long getSettingId() {
        return settingId;
    }

    public void setSettingId(Long settingId) {
        this.settingId = settingId;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
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

    public PreferenceVisibility getTaggingEnable() {
        return taggingEnable;
    }

    public void setTaggingEnable(PreferenceVisibility taggingEnable) {
        this.taggingEnable = taggingEnable;
    }

    public PreferenceVisibility getMentionEnable() {
        return mentionEnable;
    }

    public void setMentionEnable(PreferenceVisibility mentionEnable) {
        this.mentionEnable = mentionEnable;
    }

    public boolean isDiscoverable() {
        return discoverable;
    }

    public void setDiscoverable(boolean discoverable) {
        this.discoverable = discoverable;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}