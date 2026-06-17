package com.loginapp.loginapp.service;

import org.mp4parser.IsoFile;
import java.io.ByteArrayInputStream;
import java.nio.channels.Channels;
import java.awt.image.BufferedImage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.loginapp.loginapp.DTO.PostFetchDTO;
import com.loginapp.loginapp.DTO.PostUploadRequest;
import com.loginapp.loginapp.DTO.PostUploadResponse;
import com.loginapp.loginapp.Utils.AuthUtils;
import com.loginapp.loginapp.Utils.CloudinaryService;
import com.loginapp.loginapp.entity.PostMedia;
import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.BlockRepo;
import com.loginapp.loginapp.repository.FollowRepo;
import com.loginapp.loginapp.repository.PostLikeRepo;
import com.loginapp.loginapp.repository.PostMediaRepo;
import com.loginapp.loginapp.repository.PostRepo;
import com.loginapp.loginapp.repository.SavedPostRepo;

import net.coobird.thumbnailator.Thumbnails;

@Service
@Transactional
public class PostService {

    // Define max file size (500MB)
    private static final long MAX_FILE_SIZE = 500 * 1024 * 1024;

    // Inject Other Files thorugh constructor
    
    private final PostRepo postRepo;
    
    private final AuthUtils authUtils;

    private final PostMediaRepo postMediaRepo;

    private final PostCategoryDetection postCategoryDetection;

    private final CloudinaryService cloudinaryService;   
    
    private final FollowRepo followRepo;

    private final BlockRepo blockRepo;

    private final PostLikeRepo postLikeRepo;

    private final SavedPostRepo savedPostRepo;

    PostService(
        PostRepo postRepo,
        AuthUtils authUtils,
        PostMediaRepo postMediaRepo,
        PostCategoryDetection postCategoryDetection,
        CloudinaryService cloudinaryService,
        FollowRepo followRepo,
        BlockRepo blockRepo,
        PostLikeRepo postLikeRepo,
        SavedPostRepo savedPostRepo
    ) {
        this.postRepo = postRepo;
        this.authUtils = authUtils;
        this.postMediaRepo = postMediaRepo;
        this.postCategoryDetection = postCategoryDetection;
        this.cloudinaryService = cloudinaryService;
        this.followRepo = followRepo;
        this.blockRepo = blockRepo;
        this.postLikeRepo = postLikeRepo;
        this.savedPostRepo = savedPostRepo;
    }

    public PostUploadResponse uploadPost(PostUploadRequest postUploadRequest) throws IOException {

        // 1️⃣ Get logged-in username from JWT
        Users user = authUtils.getLoggedUser();

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

        // ✅ Cloudinary 
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


    // Fetch Post 
    public PostFetchDTO fetchPost(Long postId) {

        // Get Logged User
        Users user = authUtils.getLoggedUser();

        // Get Post Details
        PostsEntity post = postRepo.findSpecificPost(postId);
        if (post == null) {
            throw new IllegalArgumentException("Post is no longer available!");
        }

        Users postOwner = post.getUserpost();

        // If it not own post
        if (!postOwner.getUserId().equals(user.getUserId())) {

            // Block check — dono side
            boolean iBlockedThem = blockRepo.existsByBlockerAndBlocked(user, postOwner);
            boolean theyBlockedMe = blockRepo.existsByBlockerAndBlocked(postOwner, user);

            if (iBlockedThem || theyBlockedMe) {
                throw new IllegalArgumentException("Post is no longer available!");
            }

            // Private account check
            if (postOwner.isStatusPrivate()) {
                boolean isFollowing = followRepo.existsByFollowerAndFollowing(user, postOwner);
                if (!isFollowing) {
                    throw new IllegalArgumentException("This account is private!");
                }
            }
        }

        // Like & Save status
        boolean isLiked = postLikeRepo.existsByPostAndUser(post, user);
        boolean isSaved = savedPostRepo.existsByUserAndPost(user, post);

        // DTO Convert
        PostFetchDTO dto = new PostFetchDTO();

        dto.setFetchPostId(String.valueOf(post.getPostId()));
        dto.setFetchFileName(post.getFileName());
        dto.setFetchPostCaption(post.getPostCaption());
        dto.setFetchPostLocation(post.getPostLocation());
        dto.setFetchUploadAt(post.getUploadAt());

        // User details
        dto.setUserId(String.valueOf(postOwner.getUserId()));
        dto.setUsername(postOwner.getUsername());
        dto.setFullname(postOwner.getFullname());
        if (postOwner.getUserData() != null) {
            dto.setProfileImage(postOwner.getUserData().getProfilePhoto());
        }
        dto.setFetchVerified(postOwner.isVerifyTag());

        // Stats
        dto.setLikeCount(post.getLikeCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setViewCount(post.getViewCount());
        dto.setSaveCount(post.getSaveCount());

        // Settings
        dto.setCommentEnable(post.getCommentEnabled());
        dto.setShareEnable(post.getShareEnabled());
        dto.setLikeVisible(post.getLikeVisible());

        // Media
        PostMedia media = post.getPostMedia();
        if (media != null) {
            dto.setWidth(media.getWidth());
            dto.setHeight(media.getHeight());
            dto.setDuration(media.getDuration());
            dto.setPostType(media.getPostType().name());
        }

        // Like/Save status
        dto.setLikedByCurrentUser(isLiked);
        dto.setSavedByCurrentUser(isSaved);

        return dto;
    }

}