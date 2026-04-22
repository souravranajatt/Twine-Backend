package com.loginapp.loginapp.service;

import org.mp4parser.IsoFile;
import java.io.ByteArrayInputStream;
import java.nio.channels.Channels;
import java.awt.image.BufferedImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.loginapp.loginapp.DTO.PostUploadRequest;
import com.loginapp.loginapp.DTO.PostUploadResponse;
import com.loginapp.loginapp.entity.PostMedia;
import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.PostMediaRepo;
import com.loginapp.loginapp.repository.PostRepo;
import com.loginapp.loginapp.repository.UsersRepo;

import net.coobird.thumbnailator.Thumbnails;

@Service
public class PostService {
    
    @Autowired
    private PostRepo postRepo;
    
    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private PostMediaRepo postMediaRepo;

    @Autowired
    private PostCategoryDetection postCategoryDetection;

    @Autowired
    private CloudinaryService cloudinaryService;    // ✅ Added

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

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
        if (!contentType.startsWith("image/") && !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("Only images or videos are allowed!");
        }

        String original = file.getOriginalFilename().toLowerCase();

        List<String> allowedImages = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".webp", ".heic", ".heif", ".gif", ".bmp"
        );
        List<String> allowedVideos = Arrays.asList(
            ".mp4", ".mov", ".avi", ".mkv", ".webm"
        );

        boolean isAllowedImage = allowedImages.stream().anyMatch(original::endsWith);
        boolean isAllowedVideo = allowedVideos.stream().anyMatch(original::endsWith);

        if (!isAllowedImage && !isAllowedVideo) {
            throw new IllegalArgumentException("Invalid file type!");
        }

        if (contentType.startsWith("image/") && !isAllowedImage) {
            throw new IllegalArgumentException("Invalid image format!");
        }
        if (contentType.startsWith("video/") && !isAllowedVideo) {
            throw new IllegalArgumentException("Invalid video format!");
        }

        if (postUploadRequest.getPostCaption().length() > 250) {
            throw new IllegalArgumentException("Caption size is too long!");
        }

        // ✅ File bytes lo
        byte[] fileBytes = file.getBytes();

        // 4️⃣ Save post entity
        PostsEntity post = new PostsEntity();
        post.setUserpost(user);
        post.setPostCaption(postUploadRequest.getPostCaption());
        post.setPostLocation(postUploadRequest.getPhotoLocation());

        if (user.getUserData() != null && user.getUserData().getTimeUser() != null
        && postUploadRequest.getPostTimelineUser() == 1) {
            post.setTimelineUser(user.getUserData().getTimeUser());
        }

        // ✅ Cloudinary pe upload karo
        String filename = "TWINE_PID" + System.currentTimeMillis() + "_" + 
                          file.getOriginalFilename();
        String fileUrl = cloudinaryService.uploadFile(fileBytes, filename, contentType);
        post.setFileName(fileUrl);   // ← Cloudinary URL save hoga

        PostsEntity postsaved = postRepo.save(post);

        // Post Metadata Store
        PostMedia postdata = new PostMedia();
        postdata.setPost(postsaved);

        if (contentType.startsWith("image/")) {
            try {
                BufferedImage bufferedImage = Thumbnails.of(new ByteArrayInputStream(fileBytes))
                    .scale(1)
                    .asBufferedImage();
                if (bufferedImage != null) {
                    postdata.setWidth(bufferedImage.getWidth());
                    postdata.setHeight(bufferedImage.getHeight());
                }
            } catch (Exception e) {
                System.out.println("Image read error: " + e.getMessage());
            }
            postdata.setPostType(PostMedia.PostType.IMAGE);
            postdata.setDuration(null);

        } else if (contentType.startsWith("video/")) {
            try {
                IsoFile isoFile = new IsoFile(
                    Channels.newChannel(new ByteArrayInputStream(fileBytes))
                );
                double duration = (double) isoFile.getMovieBox()
                    .getMovieHeaderBox().getDuration() /
                    isoFile.getMovieBox()
                    .getMovieHeaderBox().getTimescale();
                isoFile.close();
                postdata.setDuration((int) duration);
            } catch (Exception e) {
                // duration nahi mila toh null rehne do
            }
            postdata.setPostType(PostMedia.PostType.VIDEO);
            postdata.setWidth(null);
            postdata.setHeight(null);
        }

        postMediaRepo.save(postdata);

        // AI Detection
        postCategoryDetection.detectAndSaveCategory(postsaved, contentType);

        // 5️⃣ Response
        PostUploadResponse response = new PostUploadResponse();
        response.setMessage("Post Uploaded!");
        return response;
    }
}