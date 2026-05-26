package com.loginapp.loginapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.loginapp.loginapp.entity.SecretCrushRelation;
import com.loginapp.loginapp.entity.Users;

import jakarta.transaction.Transactional;

public interface SecretCrushRepo extends JpaRepository<SecretCrushRelation, Long>  {
    
    void deleteByUserOneAndUserTwo(Users userOne, Users userTwo);

    boolean existsByUserOneAndUserTwo(Users userOne, Users userTwo);

    @Modifying
    @Transactional
    @Query("DELETE FROM SecretCrushRelation s WHERE s.userOne = :user OR s.userTwo = :user")
    void deleteByUser(@Param("user") Users user);
    
}
