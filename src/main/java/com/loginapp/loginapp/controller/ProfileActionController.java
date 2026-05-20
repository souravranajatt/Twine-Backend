package com.loginapp.loginapp.controller;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loginapp.loginapp.DTO.BlockRequestDTO;
import com.loginapp.loginapp.DTO.FollowRequest;
import com.loginapp.loginapp.service.ProfileActionService;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/v1")
public class ProfileActionController {
    
    @Autowired
    private ProfileActionService profileActionService;


    // Follow/Unfollow User Endpoint
    @PostMapping("/user/follow")
    public ResponseEntity<?> followButtonAction(@RequestBody FollowRequest followRequest){
        try {
            profileActionService.followUserAction(followRequest);
            return ResponseEntity.ok("Success");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Block/Unblock User Endpoint
    @PostMapping("/user/block")
    public ResponseEntity<?> blockUserAction(@RequestBody BlockRequestDTO blockRequestDTO) {
        try {
            String result = profileActionService.blockUserAction(blockRequestDTO);
            return ResponseEntity.ok(result);
        }catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
    
}
