package com.loginapp.loginapp.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordHashing {
    
    @Autowired
    private BCryptPasswordEncoder encoder;
    
    // Hash password with BCrypt
    public String hashPassword(String password) {
        return encoder.encode(password);
    }

    // Verify raw password against stored BCrypt hash
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }
}
