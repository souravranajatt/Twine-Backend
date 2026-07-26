package com.loginapp.loginapp.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name="signup_verification", indexes = {
    @Index(name = "idx_signup_verif_email", columnList = "email_id")
})
public class SignupVerification {

    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(
        name = "snowflake",
        strategy = "com.loginapp.loginapp.Utils.SnowflakeIdGenerator"
    )
    @Column(name = "verification_id", nullable = false, unique = true)
    private Long verificationId;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Column(name = "email_id", nullable = false)
    private String emailId;

    @Column(name="is_verified", nullable = false)
    private boolean isVerified = false;

    @Column(name="attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expired_at")
    private LocalDateTime expireAt;

    public SignupVerification() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    }


    // Getter & Setter 

    public Long getVerificationId() {
        return verificationId;
    }
    public void setVerificationId(Long verificationId) {
        this.verificationId = verificationId;
    }
    public String getOtpHash() {
        return otpHash;
    }
    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }
    public String getEmailId() {
        return emailId;
    }
    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }
    public boolean isVerified() {
        return isVerified;
    }
    public void setVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }
    public LocalDateTime getExpireAt() {
        return expireAt;
    }
    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }
    public int getAttemptCount() {
        return attemptCount;
    }
    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    
}
