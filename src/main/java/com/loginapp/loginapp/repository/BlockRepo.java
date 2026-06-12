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



    // Get blocked and blocking Ids
    @Query("SELECT b.blocked FROM BlockUser b WHERE b.blocker = :user")
    List<Users> findBlockedUsers(@Param("user") Users user); // Blocked by Logged User

    @Query("SELECT b.blocker FROM BlockUser b WHERE b.blocked = :user")
    List<Users> findBlockedByUsers(@Param("user") Users user); // Loggeed User Blocked by Others
}
