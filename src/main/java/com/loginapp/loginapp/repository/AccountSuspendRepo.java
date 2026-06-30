package com.loginapp.loginapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loginapp.loginapp.entity.AccountSuspend;
import com.loginapp.loginapp.entity.Users;

public interface AccountSuspendRepo extends JpaRepository<AccountSuspend, Long>{

    // Find Valid Suspend Account
    AccountSuspend findTopByUserAndIsValidOrderBySuspendedAtDesc(Users user, boolean isValid);
}
