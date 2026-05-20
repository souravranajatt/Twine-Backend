package com.loginapp.loginapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loginapp.loginapp.entity.BlockUser;
import com.loginapp.loginapp.entity.Users;

public interface BlockRepo extends JpaRepository<BlockUser, Long> {

    // Custom query to check if a block relationship exists between two users
    boolean existsByBlockerAndBlocked(Users blocker, Users blocked);
    
    // Custom query to find a block relationship between two users
    BlockUser findByBlockerAndBlocked(Users blocker, Users blocked);

    // Custom query to delete a block relationship between two users
    void deleteByBlockerAndBlocked(Users blocker, Users blocked);
}
