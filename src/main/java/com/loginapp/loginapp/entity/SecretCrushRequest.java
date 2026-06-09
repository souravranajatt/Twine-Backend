package com.loginapp.loginapp.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;

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
@Table(name = "secret_crush_request",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sender_id", "anonymous_id"})
    }
)
public class SecretCrushRequest {
    
    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(
        name = "snowflake",
        strategy = "com.loginapp.loginapp.Utils.SnowflakeIdGenerator" // fixed package
    )
    @Column(name = "request_id", nullable = false, unique = true)
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Users senderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anonymous_id", nullable = false)
    private Users anonymousId;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    public SecretCrushRequest() {}

    @PrePersist
    protected void onCreate() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        }
    }

    // Getters and Setters

    public Long getRequestId() {
        return requestId;
    }
    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }
    public Users getSenderId() {
        return senderId;
    }
    public void setSenderId(Users senderId) {
        this.senderId = senderId;
    }
    public Users getAnonymousId() {
        return anonymousId;
    }
    public void setAnonymousId(Users anonymousId) {
        this.anonymousId = anonymousId;
    }
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
}
