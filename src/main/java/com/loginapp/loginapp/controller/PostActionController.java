package com.loginapp.loginapp.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loginapp.loginapp.DTO.PostCommentDTO;
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
        }catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage()); // 403 not 400
        }catch(Exception e){
            return ResponseEntity.status(500).body("An error occurred while updating the view count.");
        }
    }

    // Archive a post
    @PatchMapping("/{postId}/archive")
    public ResponseEntity<?> archivePost(@PathVariable Long postId) {
        try {
            postActionService.archivePost(postId);
            return ResponseEntity.ok("Archived");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage()); // 403 not 400
        }catch(Exception e){
            return ResponseEntity.status(500).body("An error occurred while updating..");
        }
    }

    // Unarchive a post
    @PatchMapping("/{postId}/unarchive")
    public ResponseEntity<?> unarchivePost(@PathVariable Long postId) {
        try {
            postActionService.unarchivePost(postId);
            return ResponseEntity.ok("Unarchived");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage()); // 403 not 400
        }catch(Exception e){
            return ResponseEntity.status(500).body("An error occurred while updating..");
        }
    }

    // Hide Like on a post
    @PatchMapping("/{postId}/hide-likes")
    public ResponseEntity<?> hideLikes(@PathVariable Long postId) {
        try {
            postActionService.hideLikes(postId);
            return ResponseEntity.ok("Likes are Hidden!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage()); // 403 not 400
        }catch(Exception e){
            return ResponseEntity.status(500).body("An error occurred while updating..");
        }
    }

    // Show Like on a post
    @PatchMapping("/{postId}/show-likes")
    public ResponseEntity<?> showLikes(@PathVariable Long postId) {
        try {
            postActionService.showLikes(postId);
            return ResponseEntity.ok("Likes are shown!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage()); // 403 not 400
        }catch(Exception e){
            return ResponseEntity.status(500).body("An error occurred while updating..");
        }
    }

    // Disable Comment on a post
    @PatchMapping("/{postId}/disable-comments")
    public ResponseEntity<?> disableComment(@PathVariable Long postId) {
        try {
            postActionService.disableComments(postId);
            return ResponseEntity.ok("Comments are disabled!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage()); // 403 not 400
        }catch(Exception e){
            return ResponseEntity.status(500).body("An error occurred while updating..");
        }
    }

    // Enable comments on a post
    @PatchMapping("/{postId}/enable-comments")
    public ResponseEntity<?> enableComment(@PathVariable Long postId) {
        try {
            postActionService.enableComments(postId);
            return ResponseEntity.ok("Comments are enabled!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage()); // 403 not 400
        }catch(Exception e){
            return ResponseEntity.status(500).body("An error occurred while updating..");
        }
    }

    // Delete a post 
    @DeleteMapping("/{postId}/delete")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        try {
            postActionService.deletePost(postId);
            return ResponseEntity.ok("Post deleted successfully!");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409).body("Cannot delete post — related data exists!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while deleting!");
        }
    }

}
