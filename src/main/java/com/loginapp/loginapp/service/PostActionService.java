package com.loginapp.loginapp.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import com.loginapp.loginapp.DTO.PostCommentDTO;
import com.loginapp.loginapp.Utils.AuthUtils;
import com.loginapp.loginapp.Utils.SocialFilterHelper;
import com.loginapp.loginapp.entity.PostComment;
import com.loginapp.loginapp.entity.PostLike;
import com.loginapp.loginapp.entity.PostSeen;
import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.SavedPosts;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.PostLikeRepo;
import com.loginapp.loginapp.repository.PostRepo;
import com.loginapp.loginapp.repository.PostSeenRepo;
import com.loginapp.loginapp.repository.SavedPostRepo;
import com.loginapp.loginapp.repository.PostCommentRepo;

@Service
@Transactional
public class PostActionService {

    private final PostCommentRepo postCommentRepo;
    private final AuthUtils authUtils;
    private final AffinityService affinityService;
    private final PostRepo postRepo;
    private final PostLikeRepo postLikeRepo;
    private final SavedPostRepo savedPostRepo;
    private final PostSeenRepo postSeenRepo;
    private final SocialFilterHelper socialFilterHelper;

    private static final ZoneId ZONE_KOLKATA = ZoneId.of("Asia/Kolkata");

    PostActionService(AuthUtils authUtils, AffinityService affinityService, PostRepo postRepo,
                      PostLikeRepo postLikeRepo, SavedPostRepo savedPostRepo,
                      PostSeenRepo postSeenRepo, PostCommentRepo postCommentRepo,
                      SocialFilterHelper socialFilterHelper) {
        this.authUtils = authUtils;
        this.affinityService = affinityService;
        this.postRepo = postRepo;
        this.postLikeRepo = postLikeRepo;
        this.savedPostRepo = savedPostRepo;
        this.postSeenRepo = postSeenRepo;
        this.postCommentRepo = postCommentRepo;
        this.socialFilterHelper = socialFilterHelper;
    }

    // Like a Post
    public void likePost(Long postId) {
        Users loggedUser = authUtils.getLoggedUser();
        PostsEntity post = socialFilterHelper.getActivePostOrThrow(postId);

        boolean alreadyLiked = postLikeRepo.existsByPostAndUser(post, loggedUser);
        if (alreadyLiked) {
            throw new IllegalArgumentException("You have already liked this post!");
        }

        PostLike postLike = new PostLike();
        postLike.setPost(post);
        postLike.setUser(loggedUser);
        postLikeRepo.save(postLike);

        postRepo.incrementLikeCount(postId);

        affinityService.updateAffinityOnLike(loggedUser, post);
    }

    // Unlike a Post
    public void unlikePost(Long postId) {
        Users loggedUser = authUtils.getLoggedUser();
        PostsEntity post = socialFilterHelper.getActivePostOrThrow(postId);

        Optional<PostLike> postLikeOptional = postLikeRepo.findByPostAndUser(post, loggedUser);
        if (postLikeOptional.isPresent()) {
            PostLike postLike = postLikeOptional.get();
            postLikeRepo.delete(postLike);

            postRepo.decrementLikeCount(postId);

            affinityService.updateAffinityOnUnlike(loggedUser, post);
        } else {
            throw new IllegalArgumentException("Something went wrong!");
        }
    }

    // Save a Post
    public void savePost(Long postId) {
        Users loggedUser = authUtils.getLoggedUser();
        PostsEntity post = socialFilterHelper.getActivePostOrThrow(postId);

        boolean alreadySaved = savedPostRepo.existsByUserAndPost(loggedUser, post);
        if (alreadySaved) {
            throw new IllegalArgumentException("You have already saved this post!");
        }

        SavedPosts savedPost = new SavedPosts();
        savedPost.setUser(loggedUser);
        savedPost.setPost(post);
        savedPostRepo.save(savedPost);

        postRepo.incrementSaveCount(postId);

        affinityService.updateAffinityOnSave(loggedUser, post);
    }

    // Unsave a Post
    public void unsavePost(Long postId) {
        Users loggedUser = authUtils.getLoggedUser();
        PostsEntity post = socialFilterHelper.getActivePostOrThrow(postId);

        Optional<SavedPosts> savedPostOptional = savedPostRepo.findByUserAndPost(loggedUser, post);
        if (savedPostOptional.isPresent()) {
            SavedPosts savedPost = savedPostOptional.get();
            savedPostRepo.delete(savedPost);

            postRepo.decrementSaveCount(postId);

            affinityService.updateAffinityOnUnsave(loggedUser, post);
        } else {
            throw new IllegalArgumentException("Something went wrong!");
        }
    }

    // View a Post 
    public void viewPost(Long postId) {
        Users loggedUser = authUtils.getLoggedUser();
        PostsEntity post = socialFilterHelper.getActivePostOrThrow(postId);

        PostSeen existingSeen = postSeenRepo.findByPostAndUser(post, loggedUser);
        LocalDateTime now = LocalDateTime.now(ZONE_KOLKATA);

        if (existingSeen != null) {
            boolean tenMinutesPassed = existingSeen.getLastViewedAt().plusMinutes(10).isBefore(now);
            if (!tenMinutesPassed) {
                return;
            }
            existingSeen.setViewCount(existingSeen.getViewCount() + 1);
            existingSeen.setLastViewedAt(now);
            postSeenRepo.save(existingSeen);
        } else {
            PostSeen postSeen = new PostSeen();
            postSeen.setPost(post);
            postSeen.setUser(loggedUser);
            postSeenRepo.save(postSeen);
        }

        postRepo.incrementViewCount(postId);
        affinityService.updateAffinityOnView(loggedUser, post);
    }

    // Comment a Post 
    public void commentPost(Long postId, PostCommentDTO commentDTO) {
        Users loggedUser = authUtils.getLoggedUser();
        PostsEntity post = socialFilterHelper.getActivePostOrThrow(postId);

        if (!post.getCommentEnabled()) {
            throw new AccessDeniedException("Invalid Actions!");
        }

        if (commentDTO == null || commentDTO.getCommentText() == null || commentDTO.getCommentText().isBlank()) {
            throw new IllegalArgumentException("Comment cannot be empty!");
        }

        if (commentDTO.getCommentText().length() > 2200) {
            throw new IllegalArgumentException("Comment cannot exceed 2200 characters!");
        }

        PostComment newComment = new PostComment();
        newComment.setCommentText(commentDTO.getCommentText().trim());
        newComment.setPost(post);
        newComment.setUser(loggedUser);

        if (commentDTO.getParentId() != null && !commentDTO.getParentId().trim().isEmpty()) {
            try {
                Long parentid = Long.parseLong(commentDTO.getParentId().trim());
                PostComment parentComment = postCommentRepo.findById(parentid)
                    .orElseThrow(() -> new IllegalArgumentException("Comment not found!"));

                // Verify parent comment belongs to the same post
                if (!parentComment.getPost().getPostId().equals(post.getPostId())) {
                    throw new IllegalArgumentException("Invalid parent comment!");
                }

                newComment.setParentId(parentComment);
                postCommentRepo.incrementReplyCount(parentid);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid parent comment ID!");
            }
        }

        postCommentRepo.save(newComment);
        postRepo.incrementCommentCount(postId);

        // Update user affinity score for comment!
        affinityService.updateAffinityOnComment(loggedUser, post);
    } 

    // Archive a post
    public void archivePost(Long postId){
        Users loggedUser = authUtils.getLoggedUser();
        PostsEntity post = socialFilterHelper.getOwnedPostOrThrow(postId, loggedUser);

        if (!post.getPostVisiblity()) {
            throw new IllegalArgumentException("Post already archived!");
        }

        post.setPostVisiblity(false);
        postRepo.save(post);
    }

    // Unarchive a post
    public void unarchivePost(Long postId){
        Users loggedUser = authUtils.getLoggedUser();

        PostsEntity post = postRepo.findArchivedPostById(postId, loggedUser);
        if (post == null) {
            throw new IllegalArgumentException("Post no longer available!");
        }

        if (post.getPostVisiblity()) {
            throw new IllegalArgumentException("Post already unarchived!");
        }

        post.setPostVisiblity(true);
        postRepo.save(post);
    }

    // Hide Likes on a post
    public void hideLikes(Long postId){
        Users loggedUser = authUtils.getLoggedUser();
        PostsEntity post = socialFilterHelper.getOwnedPostOrThrow(postId, loggedUser);

        if (!post.getLikeVisible()) {
            throw new IllegalArgumentException("Likes already hidden!");
        }

        post.setLikeVisible(false);
        postRepo.save(post);
    } 

    // Show Likes on a post
    public void showLikes(Long postId){
        Users loggedUser = authUtils.getLoggedUser();
        PostsEntity post = socialFilterHelper.getOwnedPostOrThrow(postId, loggedUser);

        if (post.getLikeVisible()) {
            throw new IllegalArgumentException("Likes already shown!");
        }

        post.setLikeVisible(true);
        postRepo.save(post);
    } 

    // Disable comments on a post
    public void disableComments(Long postId){
        Users loggedUser = authUtils.getLoggedUser();
        PostsEntity post = socialFilterHelper.getOwnedPostOrThrow(postId, loggedUser);

        if (!post.getCommentEnabled()) {
            throw new IllegalArgumentException("Comments already disabled!");
        }

        post.setCommentEnabled(false);
        postRepo.save(post);
    } 

    // Enable comments on a post
    public void enableComments(Long postId){
        Users loggedUser = authUtils.getLoggedUser();
        PostsEntity post = socialFilterHelper.getOwnedPostOrThrow(postId, loggedUser);

        if (post.getCommentEnabled()) {
            throw new IllegalArgumentException("Comments already enabled!");
        }

        post.setCommentEnabled(true);
        postRepo.save(post);
    }

    // Delete a Post
    public String deletePost(Long postId){
        Users loggedUser = authUtils.getLoggedUser();
        PostsEntity post = socialFilterHelper.getOwnedPostOrThrow(postId, loggedUser);

        postCommentRepo.deleteForSpecificPost(post);
        savedPostRepo.deleteForSpecificPost(post);
        postLikeRepo.deleteForSpecificPost(post);
        postSeenRepo.deleteForSpecificPost(post);

        postRepo.delete(post);
        return "Post Deleted!";
    }

}