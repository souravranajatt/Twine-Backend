package com.loginapp.loginapp.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loginapp.loginapp.DTO.PostFetchDTO;
import com.loginapp.loginapp.DTO.PostUploadRequest;
import com.loginapp.loginapp.DTO.PostUploadResponse;
import com.loginapp.loginapp.service.PostService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/post")
public class PostController {
    
    private final PostService postService;

    PostController(PostService postService) {
        this.postService = postService;
    }

    // Upload Post 
    @PostMapping("/uploadpost")
    public ResponseEntity<PostUploadResponse> postUploadLive(@ModelAttribute PostUploadRequest postUploadRequest) {
        try{
            PostUploadResponse finalRes = postService.uploadPost(postUploadRequest);
            return ResponseEntity.ok(finalRes);
        }catch(IllegalArgumentException e){
            PostUploadResponse errRes = new PostUploadResponse();
            errRes.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errRes);
        }catch(IOException e){
            PostUploadResponse errRes = new PostUploadResponse();
            errRes.setMessage("File upload failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errRes);
        }
    }

    // Fetch Post 
    @GetMapping("/{postId}")
    public ResponseEntity<?> postFetch(@PathVariable Long postId) {
        try {
            PostFetchDTO post = postService.fetchPost(postId);
            return ResponseEntity.ok(post);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
    
}
