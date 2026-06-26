package com.loginapp.loginapp.Utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordHashing {
    
    private final BCryptPasswordEncoder encoder;

    PasswordHashing(BCryptPasswordEncoder encoder) {
        this.encoder = encoder;
    }
    
    // Hash password with BCrypt
    public String hashPassword(String password) {
        return encoder.encode(password);
    }

    // Verify raw password against stored BCrypt hash
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }
}
