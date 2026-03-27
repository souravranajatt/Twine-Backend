package com.loginapp.loginapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.io.IOException;

import com.loginapp.loginapp.DTO.PostUploadRequest;
import com.loginapp.loginapp.DTO.PostUploadResponse;
import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.PostRepo;
import com.loginapp.loginapp.repository.UsersRepo;

@Service
public class PostService {
    
    @Autowired
    private PostRepo postRepo;
    
    @Autowired
    private UsersRepo usersRepo;

    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";
    private static final long MAX_FILE_SIZE = 500 * 1024 * 1024; // 50MB


    // Post Upload Logic
    public PostUploadResponse uploadPost(PostUploadRequest postUploadRequest) throws IOException {

        // 1️⃣ Get logged-in username from JWT
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userUid = Long.parseLong(userIdStr);
        Users user = usersRepo.findByUserId(userUid)
                              .orElseThrow(() -> new IllegalArgumentException("Something went wrong!"));

        // 2️⃣ Get the file
        MultipartFile file = postUploadRequest.getFile();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Select a photo or video!");
        }

        // 3️⃣ File validations
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size must be less than 50MB!");
        }

        String contentType = file.getContentType();
        if(!contentType.startsWith("image/") && !contentType.startsWith("video/")){
            throw new IllegalArgumentException("Only images or videos are allowed!");
        }

        String original = file.getOriginalFilename().toLowerCase();
        if (original.endsWith(".exe") || original.endsWith(".apk") || original.endsWith(".sh")) {
            throw new IllegalArgumentException("Invalid file type!");
        }

        if(postUploadRequest.getPostCaption().length() > 250){
            throw new IllegalArgumentException("Caption size is too long!");
        }

        // 4️⃣ Ensure upload folder exists
        File folder = new File(uploadDir);
        if (!folder.exists()) folder.mkdirs();

        // 5️⃣ Save post entity
        PostsEntity post = new PostsEntity();
        post.setUserpost(user); // set user from JWT
        post.setPostCaption(postUploadRequest.getPostCaption());
        post.setPostLocation(postUploadRequest.getPhotoLocation());

        // Check Timeline add or not safely
        if(user.getUserData() != null && user.getUserData().getTimeUser() != null 
        && postUploadRequest.getPostTimelineUser() == 1) {
            post.setTimelineUser(user.getUserData().getTimeUser());
        }

        // Unique filename
        String filename = "TWINE_PID" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File destFile = new File(folder, filename);
        file.transferTo(destFile);
        post.setFileName(filename);

        postRepo.save(post);

        // 6️⃣ Response
        PostUploadResponse response = new PostUploadResponse();
        response.setMessage("Post Uploaded!");
        return response;
    }
}
