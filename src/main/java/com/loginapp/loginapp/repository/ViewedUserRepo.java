package com.loginapp.loginapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loginapp.loginapp.entity.ViewedUser;

public interface ViewedUserRepo extends JpaRepository<ViewedUser, Long> {
   
    
}
