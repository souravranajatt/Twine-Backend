package com.loginapp.loginapp.controller;

import java.util.List;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.loginapp.loginapp.DTO.PostFetchDTO;
import com.loginapp.loginapp.service.HomeFeedService;

@RestController
@RequestMapping("/api")
public class HomeFeedController {

    @Autowired
    private HomeFeedService homeFeedService;

    // ✅ Home Feed API
    @GetMapping("/feed")
    public ResponseEntity<List<PostFetchDTO>> getHomeFeed(
            @RequestParam(defaultValue = "0") int page) {

        try {
            List<PostFetchDTO> finalResponse = homeFeedService.getHomeFeed(page);
            return ResponseEntity.ok(finalResponse);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());

        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        }
    }
}