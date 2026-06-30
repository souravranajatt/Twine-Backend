package com.loginapp.loginapp.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;

@Entity
@Table(name = "account_suspensions")
public class AccountSuspend {

    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(
        name = "snowflake",
        strategy = "com.loginapp.loginapp.Utils.SnowflakeIdGenerator"
    )
    @Column(name = "suspension_id", nullable = false, unique = true)
    private Long suspensionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "reason", length = 500, nullable = false)
    private String reason;

    @Column(name = "is_valid", nullable = false, updatable = true)
    private boolean isValid = true;

    @Column(name = "suspended_at", nullable = false, updatable = false)
    private LocalDateTime suspendedAt;

    @Column(name = "suspended_until", nullable = false)
    private LocalDateTime suspendedUntil; 

    public AccountSuspend() {
    }

    @PrePersist
    protected void onCreate() {
        this.suspendedAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    }

    // Getters & Setters
    public Long getSuspensionId() {
        return suspensionId;
    }

    public void setSuspensionId(Long suspensionId) {
        this.suspensionId = suspensionId;
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

    public LocalDateTime getSuspendedAt() {
        return suspendedAt;
    }

    public LocalDateTime getSuspendedUntil() {
        return suspendedUntil;
    }

    public void setSuspendedUntil(LocalDateTime suspendedUntil) {
        this.suspendedUntil = suspendedUntil;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }
    
}