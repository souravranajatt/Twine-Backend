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

    // Find all blocked user by current user
    @Query("SELECT b FROM BlockUser b WHERE b.blocker = :blocker")
    List<BlockUser> findByBlocker(@Param("blocker") Users blocker);

    // Find all blocked user by other user
    @Query("SELECT b FROM BlockUser b WHERE b.blocked = :blocked")
    List<BlockUser> findByBlocked(@Param("blocked") Users blocked);

    @Query("SELECT b.blocked FROM BlockUser b WHERE b.blocker = :user")
    List<Users> findBlockedUsers(@Param("user") Users user);

    @Query("SELECT b.blocker FROM BlockUser b WHERE b.blocked = :user")
    List<Users> findBlockedByUsers(@Param("user") Users user);
}
