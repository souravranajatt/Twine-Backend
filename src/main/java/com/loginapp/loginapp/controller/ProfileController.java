package com.loginapp.loginapp.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.loginapp.loginapp.DTO.LoggedUserResponse;
import com.loginapp.loginapp.DTO.PostFetchDTO;
import com.loginapp.loginapp.DTO.SearchUserResponse;
import com.loginapp.loginapp.service.ProfileService;



@RestController
@RequestMapping("/api")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    // Search User Profile Fetch End Point ...
    @GetMapping("/profile/{username}")
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
    @GetMapping("/profile/{username}/post")
    public ResponseEntity<List<PostFetchDTO>> getUserPost(@PathVariable String username,@RequestParam(defaultValue = "0") int page) {
        try{
            List<PostFetchDTO> finalRes = profileService.getSearchUserPosts(username.toLowerCase(), page);
            return ResponseEntity.ok(finalRes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    // Serach User Timeline Post Data 
    @GetMapping("/profile/{username}/timeline")
    public ResponseEntity<List<PostFetchDTO>> getTimelinePost(@PathVariable String username,@RequestParam(defaultValue = "0") int page) {
        try{
            List<PostFetchDTO> finalRes = profileService.getSearchUserTimelinePosts(username.toLowerCase(), page);
            return ResponseEntity.ok(finalRes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }
    

    // Logged User Profile Fetch End Point
    @GetMapping("/profile/data/loggeduser")    
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


}
