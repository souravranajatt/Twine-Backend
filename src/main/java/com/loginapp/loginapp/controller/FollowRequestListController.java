package com.loginapp.loginapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.loginapp.loginapp.DTO.FollowRequestListDTO;
import com.loginapp.loginapp.service.FollowRequestListService;

@RestController
@RequestMapping("/api")
public class FollowRequestListController {

    private final FollowRequestListService followRequestListService;

    public FollowRequestListController(FollowRequestListService followRequestListService) {
        this.followRequestListService = followRequestListService;
    }

    // Endpoint to get follow requests for the logged-in user
    @GetMapping("/follow-requests")
    public ResponseEntity<List<FollowRequestListDTO>> getFollowRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        try {
            List<FollowRequestListDTO> followRequests = followRequestListService.handleFollowRequest(page, size);
            return ResponseEntity.ok(followRequests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
