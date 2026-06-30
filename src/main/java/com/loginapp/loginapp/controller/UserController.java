package com.loginapp.loginapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.loginapp.loginapp.DTO.LoginRequest;
import com.loginapp.loginapp.DTO.LoginResponse;
import com.loginapp.loginapp.DTO.SignupRequest;
import com.loginapp.loginapp.DTO.SignupResponse;
import com.loginapp.loginapp.Utils.JwtUtils;
import com.loginapp.loginapp.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    private final JwtUtils jwtUtils;

    @Value("${app.secure-cookie}")
    private boolean secureCookie;

    UserController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    // Signup endpoint
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest signupRequest, HttpServletResponse response) {
        try {
            SignupResponse responseFinal = userService.registerUser(signupRequest);
            
            // 1️⃣ Get the actual JWT token before nulling
            String token = responseFinal.getJwtToken();

            // 2️⃣ Save token to HTTPOnly cookie
            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(secureCookie);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60);
            cookie.setAttribute("SameSite", "Lax");
            response.addCookie(cookie);

            // 3️⃣ Hide JWT from frontend
            responseFinal.setJwtToken(null);

            return ResponseEntity.ok(responseFinal);
        } catch (IllegalArgumentException e) {
            SignupResponse errResponse = new SignupResponse();
            errResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errResponse);
        }
    }


    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            LoginResponse responseFinal = userService.loginUser(loginRequest);

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

            // Suspended message check karke 403 bhej sakte ho
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
        SecurityContextHolder.clearContext();
        // Delete the cookie
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie); 
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);

        return ResponseEntity.ok("Logged out successfully");
    }


}