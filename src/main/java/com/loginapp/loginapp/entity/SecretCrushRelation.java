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
@Table(name = "secret_crush_relation", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_one", "user_two"})
    }
)
public class SecretCrushRelation {

    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(
        name = "snowflake",
        strategy = "com.loginapp.loginapp.Utils.SnowflakeIdGenerator" 
    )
    @Column(name = "relation_id", nullable = false, unique = true)
    private Long relationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_one", nullable = false)
    private Users userOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_two", nullable = false)
    private Users userTwo;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }

    // Getters and Setters

    public Long getRelationId() {
        return relationId;
    }

    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }

    public Users getUserOne() {
        return userOne;
    }

    public void setUserOne(Users userOne) {
        this.userOne = userOne;
    }

    public Users getUserTwo() {
        return userTwo;
    }

    public void setUserTwo(Users userTwo) {
        this.userTwo = userTwo;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }    

}
