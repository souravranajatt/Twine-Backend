package com.loginapp.loginapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import com.loginapp.loginapp.DTO.TaggingResult;
import com.loginapp.loginapp.DTO.UserSearchDTO;
import com.loginapp.loginapp.annotation.RateLimit;
import com.loginapp.loginapp.service.SearchService;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    // User Search Endpoint
    @GetMapping("/users")
    @RateLimit(key = "SEARCH_USERS", maxRequests = 40, windowSeconds = 60, message = "Too many search requests. Please slow down.")
    public ResponseEntity<List<UserSearchDTO>> searchUsers(@RequestParam String query) {
        try {
            List<UserSearchDTO> results = searchService.searchUsers(query);
            return ResponseEntity.ok(results);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
        
    }

    // User Search for Tagging Endpoint
    @GetMapping("/tagging")
    @RateLimit(key = "SEARCH_TAGGING", maxRequests = 40, windowSeconds = 60, message = "Too many search requests. Please slow down.")
    public ResponseEntity<List<TaggingResult>> searchUsersForTagging(@RequestParam String query) {
        try {
            List<TaggingResult> results = searchService.searchUsersForTagging(query);
            return ResponseEntity.ok(results);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
