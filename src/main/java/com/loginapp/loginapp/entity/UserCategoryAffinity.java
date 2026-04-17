package com.loginapp.loginapp.entity;

import java.time.LocalDateTime;
import org.hibernate.annotations.GenericGenerator;
import jakarta.persistence.*;

@Entity
@Table(
    name = "user_category_affinity",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category"}),
    indexes = {
        @Index(name = "idx_affinity_user_id", columnList = "user_id"),
        @Index(name = "idx_affinity_score", columnList = "affinity_score")
    }
)
public class UserCategoryAffinity {

    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(
        name = "snowflake",
        strategy = "com.loginapp.loginapp.Utils.SnowflakeIdGenerator"
    )
    @Column(name = "affinity_id")
    private Long affinityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "category", length = 50, nullable = false)
    private String category;

    @Column(name = "affinity_score")
    private Float affinityScore = 0.0f;

    @Column(name = "interaction_count")
    private Integer interactionCount = 0;

    @Column(name = "last_interacted_at")
    private LocalDateTime lastInteractedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { this.updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    // Getters & Setters
    public Long getAffinityId() { return affinityId; }
    public void setAffinityId(Long affinityId) { this.affinityId = affinityId; }
    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Float getAffinityScore() { return affinityScore; }
    public void setAffinityScore(Float affinityScore) { this.affinityScore = affinityScore; }
    public Integer getInteractionCount() { return interactionCount; }
    public void setInteractionCount(Integer interactionCount) { this.interactionCount = interactionCount; }
    public LocalDateTime getLastInteractedAt() { return lastInteractedAt; }
    public void setLastInteractedAt(LocalDateTime lastInteractedAt) { this.lastInteractedAt = lastInteractedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}