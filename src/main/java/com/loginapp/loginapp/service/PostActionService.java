package com.loginapp.loginapp.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loginapp.loginapp.Utils.AuthUtils;
import com.loginapp.loginapp.entity.PostLike;
import com.loginapp.loginapp.entity.PostSeen;
import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.SavedPosts;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.PostLikeRepo;
import com.loginapp.loginapp.repository.PostRepo;
import com.loginapp.loginapp.repository.PostSeenRepo;
import com.loginapp.loginapp.repository.SavedPostRepo;

@Service
@Transactional
public class PostActionService {

    // Inject Other Files thorugh constructor

    private final AuthUtils authUtils;

    private final AffinityService affinityService;
    
    private final PostRepo postRepo;

    private final PostLikeRepo postLikeRepo;

    private final SavedPostRepo savedPostRepo;

    private final PostSeenRepo postSeenRepo;

    PostActionService(AuthUtils authUtils, AffinityService affinityService, PostRepo postRepo, PostLikeRepo postLikeRepo, SavedPostRepo savedPostRepo, PostSeenRepo postSeenRepo) {
        this.authUtils = authUtils;
        this.affinityService = affinityService;
        this.postRepo = postRepo;
        this.postLikeRepo = postLikeRepo;
        this.savedPostRepo = savedPostRepo;
        this.postSeenRepo = postSeenRepo;
    }

    // Like a Post
    public void likePost(Long postId) {
        // Get logged user details from security context
        Users loggedUser = authUtils.getLoggedUser();

        // Get post details using postId
        PostsEntity post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found!"));

        // check post is archive or not 
        if(!post.getPostVisiblity()){
            throw new IllegalArgumentException("Post not found!");
        }

        // Check if the user has already liked the post
        boolean alreadyLiked = postLikeRepo.existsByPostAndUser(post, loggedUser);
        if (alreadyLiked) {
            throw new IllegalArgumentException("You have already liked this post!");
        }

        // Implementation to like a post
        PostLike postLike = new PostLike();
        postLike.setPost(post);
        postLike.setUser(loggedUser);
        postLikeRepo.save(postLike);

        // Increment the like count of the post
        post.setLikeCount(post.getLikeCount() + 1);
        postRepo.save(post);

        // Update affinity 
        affinityService.updateAffinityOnLike(loggedUser, post);

    }


    // Unlike a Post
    public void unlikePost(Long postId) {
        // Get logged user details from security context
        Users loggedUser = authUtils.getLoggedUser();

        // Get post details using postId
        PostsEntity post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found!"));

        // check post is archive or not
        if(!post.getPostVisiblity()){
            throw new IllegalArgumentException("Post not found!");
        }

        // Implementation to unlike a post
        Optional<PostLike> postLikeOptional = postLikeRepo.findByPostAndUser(post, loggedUser);
        if (postLikeOptional.isPresent()) {
            PostLike postLike = postLikeOptional.get();
            postLikeRepo.delete(postLike);
            // Decrement the like count of the post
            post.setLikeCount(post.getLikeCount() - 1);
            postRepo.save(post);

            // Update affinity
            affinityService.updateAffinityOnUnlike(loggedUser, post);
        } else {
            throw new IllegalArgumentException("Something went wrong!");
        }
    }

    

    // Save a Post
    public void savePost(Long postId) {
        // Get logged user details from security context
        Users loggedUser = authUtils.getLoggedUser();

        // Get post details using postId
        PostsEntity post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found!"));

        // check post is archive or not
        if(!post.getPostVisiblity()){
            throw new IllegalArgumentException("Post not found!");
        }

        // Check if the user has already saved the post
        boolean alreadySaved = savedPostRepo.existsByUserAndPost(loggedUser, post);
        if (alreadySaved) {
            throw new IllegalArgumentException("You have already saved this post!");
        }

        // Implementation to save a post
        SavedPosts savedPost = new SavedPosts();
        savedPost.setUser(loggedUser);
        savedPost.setPost(post);
        savedPostRepo.save(savedPost);

        // Increment the save count of the post
        post.setSaveCount(post.getSaveCount() + 1);
        postRepo.save(post);

        // Update affinity
        affinityService.updateAffinityOnSave(loggedUser, post);
    }

    // Unsave a Post
    public void unsavePost(Long postId) {

        // Get logged user details from security context
        Users loggedUser = authUtils.getLoggedUser();

        // Get post details using postId
        PostsEntity post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found!"));    

        // check post is archive or not
        if(!post.getPostVisiblity()){
            throw new IllegalArgumentException("Post not found!");
        }

        // Implementation to unsave a post
        Optional<SavedPosts> savedPostOptional = savedPostRepo.findByUserAndPost(loggedUser, post);
        if (savedPostOptional.isPresent()) {
            
            SavedPosts savedPost = savedPostOptional.get();
            savedPostRepo.delete(savedPost);

            // Decrement the save count of the post
            post.setSaveCount(post.getSaveCount() - 1);
            postRepo.save(post);

            // Update affinity
            affinityService.updateAffinityOnUnsave(loggedUser, post);
        } else {
            throw new IllegalArgumentException("Something went wrong!");
        }

    }


    // View a Post 
    public void viewPost(Long postId) {
        
        // Get logged user
        Users loggedUser = authUtils.getLoggedUser();

        // Get post
        PostsEntity post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found!"));

        // Check archived
        if(!post.getPostVisiblity()){
            throw new IllegalArgumentException("Post not found!");
        }

        // Check already seen
        PostSeen existingSeen = postSeenRepo.findByPostAndUser(post, loggedUser);

        if(existingSeen != null){
    
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
            LocalDateTime lastViewed = existingSeen.getLastViewedAt();
            
            // 10 minute check
            boolean tenMinutesPassed = lastViewed.plusMinutes(10).isBefore(now);
            
            if(tenMinutesPassed){
                existingSeen.setViewCount(existingSeen.getViewCount() + 1);
                existingSeen.setLastViewedAt(now);
                postSeenRepo.save(existingSeen);

                post.setViewCount(post.getViewCount() + 1);
                postRepo.save(post);

                affinityService.updateAffinityOnView(loggedUser, post);
            } else {
                return;
            }

        } else {
            // Create new seen record
            PostSeen postSeen = new PostSeen();
            postSeen.setPost(post);
            postSeen.setUser(loggedUser);
            postSeenRepo.save(postSeen);

            post.setViewCount(post.getViewCount() + 1);
            postRepo.save(post);

            affinityService.updateAffinityOnView(loggedUser, post);
        }
    }

    
}
