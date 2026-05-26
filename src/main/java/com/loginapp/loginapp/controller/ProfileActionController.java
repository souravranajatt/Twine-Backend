package com.loginapp.loginapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loginapp.loginapp.service.ProfileActionService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/v1")
public class ProfileActionController {
    
    @Autowired
    private ProfileActionService profileActionService;


    // Follow/Unfollow User Endpoint
    @PostMapping("/user/follow/{targetUserId}")
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
    @DeleteMapping("/user/unfollow/{targetUserId}")
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
    @DeleteMapping("/user/follow/cancel/{targetUserId}")
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

    // Block User Endpoint
    @PostMapping("/user/block/{targetUserId}")
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
    @DeleteMapping("/user/unblock/{targetUserId}")
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
    @PostMapping("/user/secret-crush/{targetUserId}")
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
