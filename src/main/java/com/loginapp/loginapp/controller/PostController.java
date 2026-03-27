package com.loginapp.loginapp.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loginapp.loginapp.DTO.PostUploadRequest;
import com.loginapp.loginapp.DTO.PostUploadResponse;
import com.loginapp.loginapp.service.PostService;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/post")
public class PostController {
    
    @Autowired
    private PostService postService;

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
    
}
