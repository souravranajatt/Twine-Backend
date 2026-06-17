package com.loginapp.loginapp.controller;

import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.loginapp.loginapp.DTO.PostCommentDTO;
import com.loginapp.loginapp.DTO.PostCommentFetchDTO;
import com.loginapp.loginapp.service.PostActionService;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/v2/posts")
public class PostActionController {

    private final PostActionService postActionService;


    PostActionController(PostActionService postActionService) {
        this.postActionService = postActionService;
    }


    // Like a Post
    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable Long postId) { 
        try{
            postActionService.likePost(postId);
            return ResponseEntity.ok("Liked!");
        }catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("An error occurred while liking the post.");
        }
    }
    
    // Unlike a Post
    @DeleteMapping("/{postId}/unlike")
    public ResponseEntity<?> unlikePost(@PathVariable Long postId) {
        try{
            postActionService.unlikePost(postId);
            return ResponseEntity.ok("Unliked!");
        }catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("An error occurred while unliking the post.");
        }
     }
    
     // Save a Post
    @PostMapping("/{postId}/save")
    public ResponseEntity<?> savePost(@PathVariable Long postId) { 
        try{
            postActionService.savePost(postId);
            return ResponseEntity.ok("Saved!");
        }catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("An error occurred while saving the post.");
        }
    }
    
    //  Unsave a Post
    @DeleteMapping("/{postId}/unsave")
    public ResponseEntity<?> unsavePost(@PathVariable Long postId) { 
        try{
            postActionService.unsavePost(postId);
            return ResponseEntity.ok("Unsaved!");
        }catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("An error occurred while unsaving the post.");
        }
    }

    // View a Post
    @PostMapping("/{postId}/view")
    public ResponseEntity<?> viewPost(@PathVariable Long postId){
        try{
            postActionService.viewPost(postId);
            return ResponseEntity.ok("Viewed!");
        }catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("An error occurred while updating the view count.");
        }
    }

    // Post a comment 
    @PostMapping("/{postId}/comment")
    public ResponseEntity<?> commentPost(@PathVariable Long postId, @RequestBody PostCommentDTO postCommentDTO) {
        try{
            postActionService.commentPost(postId, postCommentDTO);
            return ResponseEntity.ok("Commented!");
        }catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("An error occurred while updating the view count.");
        }
    }


    // Comment Fetch Endpoint
    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> fetchComments(
        @PathVariable Long postId,
        @RequestParam(defaultValue = "0") int page
    ) {
        try {
            List<PostCommentFetchDTO> comments = postActionService.fetchComment(postId, page);
            return ResponseEntity.ok(comments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
    

}
