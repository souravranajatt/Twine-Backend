package com.loginapp.loginapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loginapp.loginapp.DTO.FollowRequest;
import com.loginapp.loginapp.service.ProfileActionService;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/action")
public class ProfileActionController {
    
    @Autowired
    private ProfileActionService profileActionService;

    @PostMapping("/follow/request/user")
    public ResponseEntity<?> followButtonAction(@RequestBody FollowRequest followRequest){
        try {
            profileActionService.followUserAction(followRequest);
            return ResponseEntity.ok("Success");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
