package com.loginapp.loginapp.repository;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.loginapp.loginapp.entity.BlockUser;
import com.loginapp.loginapp.entity.Users;

public interface BlockRepo extends JpaRepository<BlockUser, Long> {

    // Custom query to check if a block relationship exists between two users
    boolean existsByBlockerAndBlocked(Users blocker, Users blocked);
    
    // Custom query to find a block relationship between two users
    BlockUser findByBlockerAndBlocked(Users blocker, Users blocked);

    // Custom query to delete a block relationship between two users
    void deleteByBlockerAndBlocked(Users blocker, Users blocked);

    // Custom query to find all users blocked by a specific user
    @Query("SELECT b.blocked FROM BlockUser b WHERE b.blocker = :user AND b.blocked.statusDeleted = false")
    List<Users> findActiveBlockedUsers(@Param("user") Users user);

    // Used in profile service for tagged posts 
    @Query("SELECT b.blocked FROM BlockUser b WHERE b.blocker = :user")
    List<Users> findBlockedUsers(@Param("user") Users user); // Blocked by Logged User

    @Query("SELECT b.blocker FROM BlockUser b WHERE b.blocked = :user")
    List<Users> findBlockedByUsers(@Param("user") Users user); // Logged User Blocked by Others

    // Used For Follower Fetch in profile service
    @Query("SELECT b.blocked.userId FROM BlockUser b WHERE b.blocker = :user")
    Set<Long> findBlockedUserIds(@Param("user") Users user); // Blocked by Logged User

    @Query("SELECT b.blocker.userId FROM BlockUser b WHERE b.blocked = :user")
    Set<Long> findBlockedByUserIds(@Param("user") Users user); // Logged User Blocked by Others
}
