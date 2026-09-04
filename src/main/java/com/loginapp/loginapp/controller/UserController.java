package com.loginapp.loginapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loginapp.loginapp.DTO.LoginRequest;
import com.loginapp.loginapp.DTO.LoginResponse;
import com.loginapp.loginapp.DTO.OtpRequestDto;
import com.loginapp.loginapp.DTO.SignupRequest;
import com.loginapp.loginapp.DTO.SignupResponse;
import com.loginapp.loginapp.Utils.AuthUtils;
import com.loginapp.loginapp.service.AuthRedisService;
import com.loginapp.loginapp.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final AuthRedisService authRedisService;
    private final AuthUtils authUtils;

    private final UserService userService;

    @Value("${app.secure-cookie}")
    private boolean secureCookie;

    UserController(UserService userService, AuthRedisService authRedisService, AuthUtils authUtils) {
        this.userService = userService;
        this.authRedisService = authRedisService;
        this.authUtils = authUtils;
    }

    // Complete Registration
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest signupRequest,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) {
        try {
            SignupResponse responseFinal = userService.registerUser(signupRequest, request);

            String token = responseFinal.getJwtToken();

            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(secureCookie);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60);
            cookie.setAttribute("SameSite", "Lax");
            response.addCookie(cookie);

            responseFinal.setJwtToken(null);

            return ResponseEntity.ok(responseFinal);
        } catch (IllegalArgumentException e) {
            SignupResponse errResponse = new SignupResponse();
            errResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errResponse);
        }
    }

    // Step 1: Validate full form input and send OTP to email
    @PostMapping("/send-otp")
    public ResponseEntity<SignupResponse> sendOtp(@RequestBody SignupRequest signupRequest) {
        try {
            userService.sendOtp(signupRequest);

            SignupResponse res = new SignupResponse();
            res.setMessage("OTP sent! Please check your email.");
            return ResponseEntity.ok(res);

        } catch (IllegalArgumentException e) {
            SignupResponse errResponse = new SignupResponse();
            errResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errResponse);
        }
    }

    // Step 2: Verify the OTP 
    @PostMapping("/verify-otp")
    public ResponseEntity<SignupResponse> verifyOtp(@RequestBody OtpRequestDto otpRequestDto) {
        try {
            userService.verifyOtp(otpRequestDto);

            SignupResponse res = new SignupResponse();
            res.setMessage("OTP verified successfully!");
            return ResponseEntity.ok(res);

        } catch (IllegalArgumentException e) {
            SignupResponse errResponse = new SignupResponse();
            errResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errResponse);
        }
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {
        try {
            LoginResponse responseFinal = userService.loginUser(loginRequest, request);

            String token = responseFinal.getJwtToken();

            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(secureCookie);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60);
            cookie.setAttribute("SameSite", "Lax");
            response.addCookie(cookie);
            
            responseFinal.setJwtToken(null);
            return ResponseEntity.ok(responseFinal);

        } catch (IllegalArgumentException e) {
            LoginResponse errLogin = new LoginResponse();
            errLogin.setMessage(e.getMessage());

            if (e.getMessage().toLowerCase().contains("suspended")) {
                return ResponseEntity.status(403).body(errLogin);
            }
            return ResponseEntity.badRequest().body(errLogin);
        }
    }

    // Authentication Check EndPoint
    @GetMapping("/check-auth")
    public ResponseEntity<?> checkAuth() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null || authentication.getPrincipal().equals("anonymousUser") || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(true);
    }

    // Logout endpoint
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        
        // Delete session before removing cookie from header 
        String sessionId = authUtils.getCurrentSessionId();
        if (sessionId != null) {
            authRedisService.deleteSession(sessionId);
        }

        SecurityContextHolder.clearContext();

        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);

        return ResponseEntity.ok("Logged out successfully");
    }

    // Logout from specific device 
    @DeleteMapping("/logout-device/{sessionId}")
    public ResponseEntity<?> logoutSpecificDevice(@PathVariable String sessionId){
        try{
            userService.logoutSessionDevice(sessionId);
            return ResponseEntity.ok("Logged out from specific device successfully");
            
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body("Invalid session ID");
        }catch(Exception e){
            return ResponseEntity.status(500).body("An error occurred while logging out from the specific device");
        }
    }
}