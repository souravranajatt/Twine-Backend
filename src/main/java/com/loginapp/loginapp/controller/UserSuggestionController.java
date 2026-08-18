package com.loginapp.loginapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.loginapp.loginapp.DTO.FollowListFetchDTO;
import com.loginapp.loginapp.service.UserSuggestionService;

@RestController
@RequestMapping("/api")
public class UserSuggestionController {
    
    private final UserSuggestionService userSuggestionService;

    public UserSuggestionController(UserSuggestionService userSuggestionService) {
        this.userSuggestionService = userSuggestionService;
    }

    // Suggestion List Endpoint
    @GetMapping("/suggestions")
    public ResponseEntity<List<FollowListFetchDTO>> getSuggestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<FollowListFetchDTO> suggestions = userSuggestionService.suggestionList(page, size);
            return ResponseEntity.ok(suggestions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
