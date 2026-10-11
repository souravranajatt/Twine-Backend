package com.loginapp.loginapp.repository;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.loginapp.loginapp.entity.BlockUser;
import com.loginapp.loginapp.entity.Users;

public interface BlockRepo extends JpaRepository<BlockUser, Long> {

    // Check if a block relationship exists between two users
    boolean existsByBlockerAndBlocked(Users blocker, Users blocked);

    // Delete a block relationship between two users
    void deleteByBlockerAndBlocked(Users blocker, Users blocked);

    // Find all active users blocked by a specific user (used in Settings)
    @Query("SELECT b.blocked FROM BlockUser b WHERE b.blocker = :user AND b.blocked.statusDeleted = false")
    List<Users> findActiveBlockedUsers(@Param("user") Users user);

    // Single-query batch fetch for all blocked user IDs in both directions (blocked by user OR blocked user)
    @Query("""
        SELECT CASE WHEN b.blocker = :user THEN b.blocked.userId ELSE b.blocker.userId END 
        FROM BlockUser b 
        WHERE b.blocker = :user OR b.blocked = :user
    """)
    Set<Long> findAllBlockedUserIds(@Param("user") Users user);

    // Two-way block check between two specific users in a single query
    @Query("""
        SELECT COUNT(b) > 0 FROM BlockUser b 
        WHERE (b.blocker = :user1 AND b.blocked = :user2) 
           OR (b.blocker = :user2 AND b.blocked = :user1)
    """)
    boolean isBlockedBetween(@Param("user1") Users user1, @Param("user2") Users user2);
}
