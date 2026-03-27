package com.loginapp.loginapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loginapp.loginapp.DTO.LoginRequest;
import com.loginapp.loginapp.DTO.LoginResponse;
import com.loginapp.loginapp.DTO.SignupRequest;
import com.loginapp.loginapp.DTO.SignupResponse;
import com.loginapp.loginapp.Utils.JwtUtils;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.UsersRepo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private JwtUtils jwtUtils;

    // Username regex (only lowercase letters, numbers, underscore)
    private static final String USERNAME_REGEX = "^[a-z0-9_.]+$";
    private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);

    // Email regex
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    // SHA-256 password hashing
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Something went wrong!", e);
        }
    }

    // Signup with validation
    @Transactional
    public SignupResponse registerUser(SignupRequest signupRequest) {

        // ====== 1. Null and Empty Checks ======
        if (signupRequest.getFullname() == null || signupRequest.getFullname().trim().isEmpty()) {
            throw new IllegalArgumentException("Fullname is required!");
        }
        if (signupRequest.getUsername() == null || signupRequest.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required!");
        }
        if (signupRequest.getEmail() == null || signupRequest.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required!");
        }
        if (signupRequest.getPassword() == null || signupRequest.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required!");
        } 

        // ====== 2. Trim and Normalize Data ======
        String fullnameFinal = signupRequest.getFullname().trim();
        String usernameFinal = signupRequest.getUsername().trim().toLowerCase();
        String emailFinal = signupRequest.getEmail().trim().toLowerCase();

        // ====== 3. Full Name Validation ======
        if (fullnameFinal.length() > 30) {
            throw new IllegalArgumentException("Fullname can't exceed 30 characters!");
        }

        // ====== 4. Username Validation ======
        if (usernameFinal.length() > 25) {
            throw new IllegalArgumentException("Username can't exceed 25 characters!");
        }
        if (!USERNAME_PATTERN.matcher(usernameFinal).matches()) {
            throw new IllegalArgumentException("Username can only contain lowercase letters, digits, '.', and '_' !");
        }

        // ====== 5. Email Validation ======
        if (!EMAIL_PATTERN.matcher(emailFinal).matches()) {
            throw new IllegalArgumentException("Enter a valid email address!");
        }

        // ====== 6. Uniqueness Check ======
        if (usersRepo.findByUsername(usernameFinal).isPresent()) {
         throw new IllegalArgumentException("Username already taken!");
        }
        if (usersRepo.findByEmail(emailFinal).isPresent()) {
            throw new IllegalArgumentException("Email already registered!");
        }

        // ====== 7. Password Validation =====
        if (signupRequest.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long!");
        }

        String passwordHashFinal = hashPassword(signupRequest.getPassword());

        // Final Store value set 
        Users user = new Users();
        user.setFullname(fullnameFinal);
        user.setUsername(usernameFinal);
        user.setEmail(emailFinal);
        user.setPasswordHash(passwordHashFinal);

        Users savedUser = usersRepo.save(user);

        // Generate JWT Token userId + Username
        String resToken = jwtUtils.generateToken(savedUser.getUserId(), savedUser.getUsername());

        SignupResponse resData = new SignupResponse();
        resData.setJwtToken(resToken);
        resData.setMessage("Signup Successful!");

        return resData;
    }




    // Login validation
    public Optional<LoginResponse> loginUser(LoginRequest loginRequest) {

        if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            return Optional.empty();
        }

        Optional<Users> userOpt = usersRepo.findByUsername(loginRequest.getUsername().toLowerCase());

        if (userOpt.isEmpty()) {
            return Optional.empty(); // username not found
        }

        // hash the given password and compare
        String hashedPassword = hashPassword(loginRequest.getPassword());
        if (userOpt.get().getPasswordHash().equals(hashedPassword)) {

            // Generate JWT Token userId + Username
            String resToken = jwtUtils.generateToken(userOpt.get().getUserId(), userOpt.get().getUsername());
            
            LoginResponse resData = new LoginResponse();
            resData.setJwtToken(resToken);
            resData.setMessage("Login Successful!");
            return Optional.of(resData);
        }

        return Optional.empty(); // password not matched
    }
    
}
