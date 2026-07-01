package com.loginapp.loginapp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loginapp.loginapp.DTO.LoginRequest;
import com.loginapp.loginapp.DTO.LoginResponse;
import com.loginapp.loginapp.DTO.SignupRequest;
import com.loginapp.loginapp.DTO.SignupResponse;
import com.loginapp.loginapp.Utils.JwtUtils;
import com.loginapp.loginapp.Utils.PasswordHashing;
import com.loginapp.loginapp.entity.AccountSuspend;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.AccountSuspendRepo;
import com.loginapp.loginapp.repository.UsersRepo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserService {

    // Inject Other Files thorugh constructor

    private final UsersRepo usersRepo;

    private final JwtUtils jwtUtils;

    private final PasswordHashing passwordHashing;

    private final AccountSuspendRepo accountSuspendRepo;

    // Username regex (only lowercase letters, numbers, underscore)
    private static final String USERNAME_REGEX = "^[a-z0-9_.]+$";
    private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);

    // Email regex
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);


    UserService(UsersRepo usersRepo, JwtUtils jwtUtils, PasswordHashing passwordHashing, AccountSuspendRepo accountSuspendRepo) {
        this.usersRepo = usersRepo;
        this.jwtUtils = jwtUtils;
        this.passwordHashing = passwordHashing;
        this.accountSuspendRepo = accountSuspendRepo;
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
        if (signupRequest.getPassword().length() > 72) {
            throw new IllegalArgumentException("Password cannot exceed 72 characters!");
        }

        String passwordHashFinal = passwordHashing.hashPassword(signupRequest.getPassword());

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
    public LoginResponse loginUser(LoginRequest loginRequest) {

        // Null and Empty Checks
        if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username or email is required!");
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required!");
        }
        if (loginRequest.getPassword().length() > 72) {
            throw new IllegalArgumentException("Invalid username or password!");
        }

        String identifier = loginRequest.getUsername().trim().toLowerCase();
        Optional<Users> userOpt;

        if (identifier.contains("@")) {
            userOpt = usersRepo.findByEmail(identifier);
        } else {
            userOpt = usersRepo.findByUsername(identifier);
        }

        Users user = userOpt.orElseThrow(() -> 
            new IllegalArgumentException("Invalid username or password!"));

        // Verify password
        if (!passwordHashing.verifyPassword(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password!");
        }

        // Suspended check
        if (user.isStatusSuspend()) {

            AccountSuspend suspension = accountSuspendRepo
                .findTopByUserAndIsValidOrderBySuspendedAtDesc(user, true);

            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

            if (suspension == null) {
                user.setStatusSuspend(false);
                usersRepo.save(user);

            } else if (suspension.getSuspendedUntil().isBefore(now)) {
                suspension.setValid(false);
                accountSuspendRepo.save(suspension);

                user.setStatusSuspend(false);
                usersRepo.save(user);

            } else {
                throw new IllegalArgumentException(
                    "Your account is suspended until " + suspension.getSuspendedUntil()
                    + ". Reason: " + suspension.getReason()
                );
            }
        }

        // Auto-reactivate account if it was deactivated
        if (user.isStatusDeleted()) {
            user.setStatusDeleted(false);
            usersRepo.save(user);
        }

        // Generate JWT Token
        String resToken = jwtUtils.generateToken(user.getUserId(), user.getUsername());

        LoginResponse resData = new LoginResponse();
        resData.setJwtToken(resToken);
        resData.setMessage("Login Successful!");

        return resData;
    }
    
}
