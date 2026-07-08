package com.loginapp.loginapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loginapp.loginapp.entity.SignupVerification;

public interface SignupVerificationRepo extends JpaRepository<SignupVerification, Long> {

    // Find OTP record by email
    Optional<SignupVerification> findByEmailId(String emailId);

    // Delete old OTP record for this email before saving a new one
    void deleteByEmailId(String emailId);
}
