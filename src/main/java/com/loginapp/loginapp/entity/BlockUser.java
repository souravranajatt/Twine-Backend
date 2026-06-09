package com.loginapp.loginapp.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "block_user",
    uniqueConstraints = @UniqueConstraint(columnNames = {"blocker_id", "blocked_id"})
)
public class BlockUser {
    
    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(
        name = "snowflake",
        strategy = "com.loginapp.loginapp.Utils.SnowflakeIdGenerator" // fixed package
    )
    @Column(name = "blockId", nullable = false, unique = true)
    private Long blockId;

    @ManyToOne
    @JoinColumn(name = "blocker_id", nullable = false)
    private Users blocker;

    @ManyToOne
    @JoinColumn(name = "blocked_id", nullable = false)
    private Users blocked;

    @Column(name = "blocked_at", nullable = false, updatable = false)
    private LocalDateTime blockedAt;

    public BlockUser() {
    }

    @PrePersist
    protected void onCreate() {
        if (blockedAt == null) {
            blockedAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        }
    }

    // Getter & Setter
    public Long getBlockId() {
        return blockId;
    }
    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }
    public Users getBlocker() {
        return blocker;
    }
    public void setBlocker(Users blocker) {
        this.blocker = blocker;
    }  
    public Users getBlocked() {
        return blocked;
    }
    public void setBlocked(Users blocked) {
        this.blocked = blocked; 
    }
    public LocalDateTime getBlockedAt() {
        return blockedAt;
    }

    
}
