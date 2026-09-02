package com.loginapp.loginapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loginapp.loginapp.DTO.LoginRequest;
import com.loginapp.loginapp.DTO.LoginResponse;
import com.loginapp.loginapp.DTO.OtpRequestDto;
import com.loginapp.loginapp.DTO.SignupRequest;
import com.loginapp.loginapp.DTO.SignupResponse;
import com.loginapp.loginapp.Utils.EmailSender;
import com.loginapp.loginapp.Utils.JwtUtils;
import com.loginapp.loginapp.Utils.PasswordHashing;
import com.loginapp.loginapp.entity.AccountSuspend;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.AccountSuspendRepo;
import com.loginapp.loginapp.repository.UsersRepo;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserService {

    private final UsersRepo usersRepo;
    private final JwtUtils jwtUtils;
    private final PasswordHashing passwordHashing;
    private final AccountSuspendRepo accountSuspendRepo;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final EmailSender emailSender;
    private final AuthRedisService authRedisService;

    @Value("${app.otp.expiry-minutes}")
    private int otpExpiryMinutes;

    // Username regex (only lowercase letters, numbers, underscore, dot)
    private static final String USERNAME_REGEX = "^[a-z0-9_.]+$";
    private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);

    // Email regex
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final String OTP_KEY_PREFIX = "SIGNUP_OTP_";

    UserService(UsersRepo usersRepo, JwtUtils jwtUtils, PasswordHashing passwordHashing,
                AccountSuspendRepo accountSuspendRepo, RedisService redisService,
                ObjectMapper objectMapper, EmailSender emailSender,
                AuthRedisService authRedisService) {
        this.usersRepo = usersRepo;
        this.jwtUtils = jwtUtils;
        this.passwordHashing = passwordHashing;
        this.accountSuspendRepo = accountSuspendRepo;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
        this.emailSender = emailSender;
        this.authRedisService = authRedisService;
    }











    // Step 1: Send OTP to email 
    @Transactional
    public void sendOtp(SignupRequest signupRequest) {

        //  1. Null and Empty Checks 
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

        //  2. Trim and Normalize Data 
        String fullnameFinal = signupRequest.getFullname().trim();
        String usernameFinal = signupRequest.getUsername().trim().toLowerCase();
        String emailFinal = signupRequest.getEmail().trim().toLowerCase();

        // 3. Full Name Validation
        if (fullnameFinal.length() > 30) {
            throw new IllegalArgumentException("Fullname can't exceed 30 characters!");
        }

        // 4. Username Validation
        if (usernameFinal.length() > 25) {
            throw new IllegalArgumentException("Username can't exceed 25 characters!");
        }
        if (!USERNAME_PATTERN.matcher(usernameFinal).matches()) {
            throw new IllegalArgumentException("Username can only contain lowercase letters, digits, '.', and '_' !");
        }
        if (usernameFinal.startsWith(".")) {
            throw new IllegalArgumentException("Username cannot start with a period!");
        }
        if (usernameFinal.endsWith(".")) {
            throw new IllegalArgumentException("Username cannot end with a period!");
        }
        if (usernameFinal.contains("..")) {
            throw new IllegalArgumentException("Username cannot have consecutive periods!");
        }

        // 5. Email Validation
        if (!EMAIL_PATTERN.matcher(emailFinal).matches()) {
            throw new IllegalArgumentException("Enter a valid email address!");
        }
        String emailLocalPart = emailFinal.split("@")[0];
        if (emailLocalPart.startsWith(".")) {
            throw new IllegalArgumentException("Email cannot start with a period!");
        }
        if (emailLocalPart.endsWith(".")) {
            throw new IllegalArgumentException("Email cannot end with a period!");
        }
        if (emailLocalPart.contains("..")) {
            throw new IllegalArgumentException("Email cannot have consecutive periods!");
        }

        // 6. Uniqueness Check
        if (usersRepo.findByUsername(usernameFinal).isPresent()) {
            throw new IllegalArgumentException("Username already taken!");
        }
        if (usersRepo.findByEmail(emailFinal).isPresent()) {
            throw new IllegalArgumentException("Email already registered!");
        }

        // 7. Password Validation
        if (signupRequest.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long!");
        }
        if (signupRequest.getPassword().length() > 72) {
            throw new IllegalArgumentException("Password cannot exceed 72 characters!");
        }

        // 8. Generate 6-digit OTP
        SecureRandom random = new SecureRandom();
        int otpCode = 100000 + random.nextInt(900000);
        String otpPlain = String.valueOf(otpCode);

        // Hash OTP
        String otpHashed = passwordHashing.hashPassword(otpPlain);

        // 9. Send OTP email
        SimpleMailMessage mail = emailSender.buildOtpMessage(emailFinal, fullnameFinal, otpPlain);
        boolean sent = emailSender.sendEmail(mail);
        if (!sent) {
            throw new IllegalArgumentException("Server Timeout, Failed to deliver OTP email.");
        }

        // 10. Save OTP data to Redis
        try {
            Map<String, Object> otpData = new HashMap<>();
            otpData.put("otpHash", otpHashed);
            otpData.put("attemptCount", 0);
            otpData.put("verified", false);

            String jsonData = objectMapper.writeValueAsString(otpData);
            redisService.setValueWithExpiry(OTP_KEY_PREFIX + emailFinal, jsonData, otpExpiryMinutes * 60L);
        } catch (Exception e) {
            System.out.println("Redis OTP Save Error: " + e.getMessage());
            throw new IllegalArgumentException("Server error. Please try again.");
        }
    }



    // Step 2: Verify the OTP code
    @SuppressWarnings("unchecked")
    public void verifyOtp(OtpRequestDto otpRequestDto) {

        if (otpRequestDto.getEmail() == null || otpRequestDto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required!");
        }
        if (otpRequestDto.getOtp() == null || otpRequestDto.getOtp().trim().isEmpty()) {
            throw new IllegalArgumentException("OTP is required!");
        }

        String emailFinal = otpRequestDto.getEmail().trim().toLowerCase();
        String otpEntered = otpRequestDto.getOtp().trim();
        String redisKey = OTP_KEY_PREFIX + emailFinal;

        // Fetch from Redis
        String cachedData = redisService.getValue(redisKey);

        if (cachedData == null) {
            throw new IllegalArgumentException("OTP has expired. Please request a new OTP.");
        }

        try {
            Map<String, Object> otpData = objectMapper.readValue(cachedData, Map.class);

            String otpHash = (String) otpData.get("otpHash");
            int attemptCount = (int) otpData.get("attemptCount");

            // Attempt check
            if (attemptCount >= MAX_OTP_ATTEMPTS) {
                redisService.deleteKey(redisKey);
                throw new IllegalArgumentException("Too many incorrect OTP attempts. Please request a new OTP.");
            }

            // Verify OTP hash
            if (!passwordHashing.verifyPassword(otpEntered, otpHash)) {
                attemptCount++;
                if (attemptCount >= MAX_OTP_ATTEMPTS) {
                    redisService.deleteKey(redisKey);
                    throw new IllegalArgumentException("Too many incorrect OTP attempts. Please request a new OTP.");
                }
                otpData.put("attemptCount", attemptCount);
                String updatedJson = objectMapper.writeValueAsString(otpData);
                redisService.setValueKeepExpire(redisKey, updatedJson);
                int remaining = MAX_OTP_ATTEMPTS - attemptCount;
                throw new IllegalArgumentException("Wrong OTP. " + remaining + " attempts remaining.");
            }

            // Mark verified
            otpData.put("verified", true);
            String verifiedJson = objectMapper.writeValueAsString(otpData);
            redisService.setValueWithExpiry(redisKey, verifiedJson, 600);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("Redis OTP Verify Error: " + e.getMessage());
            throw new IllegalArgumentException("Server error. Please try again.");
        }
    }

    // Step 3: Complete registration after OTP verification
    @Transactional
    public SignupResponse registerUser(SignupRequest signupRequest, HttpServletRequest request) {

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
        if (usernameFinal.startsWith(".")) {
            throw new IllegalArgumentException("Username cannot start with a period!");
        }
        if (usernameFinal.endsWith(".")) {
            throw new IllegalArgumentException("Username cannot end with a period!");
        }
        if (usernameFinal.contains("..")) {
            throw new IllegalArgumentException("Username cannot have consecutive periods!");
        }

        // ====== 5. Email Validation ======
        if (!EMAIL_PATTERN.matcher(emailFinal).matches()) {
            throw new IllegalArgumentException("Enter a valid email address!");
        }
        String emailLocalPart = emailFinal.split("@")[0];
        if (emailLocalPart.startsWith(".")) {
            throw new IllegalArgumentException("Email cannot start with a period!");
        }
        if (emailLocalPart.endsWith(".")) {
            throw new IllegalArgumentException("Email cannot end with a period!");
        }
        if (emailLocalPart.contains("..")) {
            throw new IllegalArgumentException("Email cannot have consecutive periods!");
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

        // Verification Check from Redis
        String redisKey = OTP_KEY_PREFIX + emailFinal;
        String cachedData = redisService.getValue(redisKey);

        if (cachedData == null) {
            throw new IllegalArgumentException("Email verification is pending. Please verify OTP first.");
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> otpData = objectMapper.readValue(cachedData, Map.class);
            boolean isVerified = (boolean) otpData.get("verified");

            if (!isVerified) {
                throw new IllegalArgumentException("Email verification is pending. Please verify OTP first.");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("Redis OTP Read Error: " + e.getMessage());
            throw new IllegalArgumentException("Server error. Please try again.");
        }

        // Create User
        String passwordHashFinal = passwordHashing.hashPassword(signupRequest.getPassword());

        Users user = new Users();
        user.setFullname(fullnameFinal);
        user.setUsername(usernameFinal);
        user.setEmail(emailFinal);
        user.setPasswordHash(passwordHashFinal);

        Users savedUser = usersRepo.save(user);

        // Delete OTP from Redis
        redisService.deleteKey(redisKey);

        // Create session in Redis
        String sessionId = authRedisService.createSession(savedUser.getUserId(), savedUser.getUsername(), request);

        // Generate JWT Token with sessionId
        String resToken = jwtUtils.generateToken(savedUser.getUserId(), savedUser.getUsername(), sessionId);

        SignupResponse resData = new SignupResponse();
        resData.setJwtToken(resToken);
        resData.setMessage("Signup Successful!");

        return resData;
    }

    // Login validation
    public LoginResponse loginUser(LoginRequest loginRequest, HttpServletRequest request) {

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

        // Create session in Redis
        String sessionId = authRedisService.createSession(user.getUserId(), user.getUsername(), request);

        // Generate JWT Token with sessionId
        String resToken = jwtUtils.generateToken(user.getUserId(), user.getUsername(), sessionId);

        LoginResponse resData = new LoginResponse();
        resData.setJwtToken(resToken);
        resData.setMessage("Login Successful!");

        return resData;
    }
}
