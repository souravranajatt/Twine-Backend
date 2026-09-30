package com.loginapp.loginapp.controller;

import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.loginapp.loginapp.DTO.FollowListFetchDTO;
import com.loginapp.loginapp.DTO.LoggedUserResponse;
import com.loginapp.loginapp.DTO.PostFetchDTO;
import com.loginapp.loginapp.DTO.SearchUserResponse;
import com.loginapp.loginapp.service.ProfileService;



@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final ProfileService profileService;

    ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // Logged User Profile Fetch End Point
    @GetMapping("/me")    
    public ResponseEntity<?> loggedUserData(){
        try{
            LoggedUserResponse finalResponse = profileService.fetchLoggedData();
            return ResponseEntity.ok(finalResponse);
        }catch(IllegalArgumentException err){
            return ResponseEntity.badRequest().body(err.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Search User Profile Fetch End Point ...
    @GetMapping("/{username}")
    public ResponseEntity<?> profileByUsername(@PathVariable String username) {
        try {
            SearchUserResponse userSummary = profileService.userProfile(username.toLowerCase());
            return ResponseEntity.ok(userSummary); // directly returns only projected fields
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Search User Profile Post Data 
    @GetMapping("/{username}/posts")
    public ResponseEntity<List<PostFetchDTO>> getUserPost(@PathVariable String username, @RequestParam(defaultValue = "0") int page) {
        try {
            List<PostFetchDTO> finalRes = profileService.getSearchUserPosts(username.toLowerCase(), page);
            return ResponseEntity.ok(finalRes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    // Search User Timeline Post Data 
    @GetMapping("/{username}/timeline")
    public ResponseEntity<List<PostFetchDTO>> getTimelinePost(@PathVariable String username, @RequestParam(defaultValue = "0") int page) {
        try {
            List<PostFetchDTO> finalRes = profileService.getSearchUserTimelinePosts(username.toLowerCase(), page);
            return ResponseEntity.ok(finalRes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    // Search User Tagged Post Data 
    @GetMapping("/{username}/tagged")
    public ResponseEntity<List<PostFetchDTO>> getTaggedPost(@PathVariable String username, @RequestParam(defaultValue = "0") int page) {
        try {
            List<PostFetchDTO> finalRes = profileService.getSearchUserTaggedPosts(username.toLowerCase(), page);
            return ResponseEntity.ok(finalRes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    // Follower List Fetch
    @GetMapping("/{targetUserId}/followers")
    public ResponseEntity<?> fetchFollowerList(@PathVariable Long targetUserId, @RequestParam(defaultValue = "0") int page) {
        try {
            List<FollowListFetchDTO> followers = profileService.followerListFetch(targetUserId, page);
            return ResponseEntity.ok(followers);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

}
