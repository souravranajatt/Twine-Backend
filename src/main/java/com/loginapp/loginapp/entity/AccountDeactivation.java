package com.loginapp.loginapp.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;

@Entity
@Table(name = "account_deactivations", indexes = {
    @Index(name = "idx_deactivation_user_id", columnList = "user_id")
})
public class AccountDeactivation {

    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(
        name = "snowflake",
        strategy = "com.loginapp.loginapp.Utils.SnowflakeIdGenerator"
    )
    @Column(name = "deactivation_id", nullable = false, unique = true)
    private Long deactivationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "deactivated_at", nullable = false, updatable = false)
    private LocalDateTime deactivatedAt;

    public AccountDeactivation() {
    }

    @PrePersist
    protected void onCreate() {
        this.deactivatedAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    }

    // Getters & Setters
    public Long getDeactivationId() {
        return deactivationId;
    }

    public void setDeactivationId(Long deactivationId) {
        this.deactivationId = deactivationId;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getDeactivatedAt() {
        return deactivatedAt;
    }
}