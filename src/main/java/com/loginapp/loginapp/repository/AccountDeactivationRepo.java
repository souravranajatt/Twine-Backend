package com.loginapp.loginapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loginapp.loginapp.entity.AccountDeactivation;

public interface AccountDeactivationRepo extends JpaRepository<AccountDeactivation, Long>{
    
}
