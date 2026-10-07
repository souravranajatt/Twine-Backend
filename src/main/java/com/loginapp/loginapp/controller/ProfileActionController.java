package com.loginapp.loginapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loginapp.loginapp.annotation.RateLimit;
import com.loginapp.loginapp.service.ProfileActionService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;



@RestController
@RequestMapping("/api/v1/users")
public class ProfileActionController {
    
    private final ProfileActionService profileActionService;


    ProfileActionController(ProfileActionService profileActionService) {
        this.profileActionService = profileActionService;
    }


    // Follow Endpoint
    @PostMapping("/{targetUserId}/follow")
    @RateLimit(key = "USER_FOLLOW", maxRequests = 20, windowSeconds = 60, message = "You're following too fast! Please wait a moment.")
    public ResponseEntity<?> followButtonAction(@PathVariable Long targetUserId){
        try {
            profileActionService.followUser(targetUserId);
            return ResponseEntity.ok("Follow Successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Unfollow
    @DeleteMapping("/{targetUserId}/unfollow")
    @RateLimit(key = "USER_FOLLOW", maxRequests = 20, windowSeconds = 60, message = "You're unfollowing too fast! Please wait a moment.")
    public ResponseEntity<?> unfollowUser(@PathVariable Long targetUserId) {
        try {
            profileActionService.unfollowUser(targetUserId);
            return ResponseEntity.ok("Unfollowed successfully!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Cancel Request
    @DeleteMapping("/{targetUserId}/follow/cancel")
    @RateLimit(key = "USER_FOLLOW", maxRequests = 20, windowSeconds = 60, message = "Too many follow actions! Please wait a moment.")
    public ResponseEntity<?> cancelFollowRequest(@PathVariable Long targetUserId) {
        try {
            profileActionService.cancelFollowRequest(targetUserId);
            return ResponseEntity.ok("Request cancelled!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Accept Request
    @PostMapping("/{targetUserId}/follow/accept")
    @RateLimit(key = "USER_FOLLOW", maxRequests = 20, windowSeconds = 60, message = "Too many follow actions! Please wait a moment.")
    public ResponseEntity<?> acceptFollowRequest(@PathVariable Long targetUserId) {
        try {
            profileActionService.acceptFollowRequest(targetUserId);
            return ResponseEntity.ok("Request accepted!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Reject Request
    @DeleteMapping("/{targetUserId}/follow/reject")
    @RateLimit(key = "USER_FOLLOW", maxRequests = 20, windowSeconds = 60, message = "Too many follow actions! Please wait a moment.")
    public ResponseEntity<?> rejectFollowRequest(@PathVariable Long targetUserId) {
        try {
            profileActionService.rejectFollowRequest(targetUserId);
            return ResponseEntity.ok("Request rejected!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Block User Endpoint
    @PostMapping("/{targetUserId}/block")
    @RateLimit(key = "USER_BLOCK", maxRequests = 20, windowSeconds = 60, message = "Too many block requests. Please wait a moment.")
    public ResponseEntity<?> blockUserAction(@PathVariable Long targetUserId) {
        try {
            String result = profileActionService.blockUserAction(targetUserId);
            return ResponseEntity.ok(result);
        }catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Unblock User Endpoint
    @DeleteMapping("/{targetUserId}/unblock")
    @RateLimit(key = "USER_BLOCK", maxRequests = 20, windowSeconds = 60, message = "Too many unblock requests. Please wait a moment.")
    public ResponseEntity<?> unblockUserAction(@PathVariable Long targetUserId) {
        try {
            String result = profileActionService.unblockUserAction(targetUserId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Send Secret Crush Request Endpoint
    @PostMapping("/{targetUserId}/crush")
    @RateLimit(key = "USER_CRUSH", maxRequests = 10, windowSeconds = 60, message = "Too many secret crush requests. Please wait a moment.")
    public ResponseEntity<?> sendSecretCrushRequest(@PathVariable Long targetUserId) {
        try {
            profileActionService.sendAnonymousLike(targetUserId);
            return ResponseEntity.ok("Success");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
    
}
