package com.loginapp.loginapp.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loginapp.loginapp.DTO.PostCommentDTO;
import com.loginapp.loginapp.DTO.PostCommentFetchDTO;
import com.loginapp.loginapp.Utils.AuthUtils;
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

    PostActionService(AuthUtils authUtils, AffinityService affinityService, PostRepo postRepo, PostLikeRepo postLikeRepo, SavedPostRepo savedPostRepo, PostSeenRepo postSeenRepo, PostCommentRepo postCommentRepo) {
        this.authUtils = authUtils;
        this.affinityService = affinityService;
        this.postRepo = postRepo;
        this.postLikeRepo = postLikeRepo;
        this.savedPostRepo = savedPostRepo;
        this.postSeenRepo = postSeenRepo;
        this.postCommentRepo = postCommentRepo;
    }

    // Like a Post
    public void likePost(Long postId) {
        Users loggedUser = authUtils.getLoggedUser();

        PostsEntity post = postRepo.findActivePost(postId);
        if (post == null) {
            throw new IllegalArgumentException("Post no longer available!");
        }

        boolean alreadyLiked = postLikeRepo.existsByPostAndUser(post, loggedUser);
        if (alreadyLiked) {
            throw new IllegalArgumentException("You have already liked this post!");
        }

        PostLike postLike = new PostLike();
        postLike.setPost(post);
        postLike.setUser(loggedUser);
        postLikeRepo.save(postLike);

        post.setLikeCount(post.getLikeCount() + 1);
        postRepo.save(post);

        affinityService.updateAffinityOnLike(loggedUser, post);
    }

    // Unlike a Post
    public void unlikePost(Long postId) {
        Users loggedUser = authUtils.getLoggedUser();

        PostsEntity post = postRepo.findActivePost(postId);
        if (post == null) {
            throw new IllegalArgumentException("Post no longer available!");
        }

        Optional<PostLike> postLikeOptional = postLikeRepo.findByPostAndUser(post, loggedUser);
        if (postLikeOptional.isPresent()) {
            PostLike postLike = postLikeOptional.get();
            postLikeRepo.delete(postLike);
            post.setLikeCount(post.getLikeCount() - 1);
            postRepo.save(post);

            affinityService.updateAffinityOnUnlike(loggedUser, post);
        } else {
            throw new IllegalArgumentException("Something went wrong!");
        }
    }

    // Save a Post
    public void savePost(Long postId) {
        Users loggedUser = authUtils.getLoggedUser();

        PostsEntity post = postRepo.findActivePost(postId);
        if (post == null) {
            throw new IllegalArgumentException("Post no longer available!");
        }

        boolean alreadySaved = savedPostRepo.existsByUserAndPost(loggedUser, post);
        if (alreadySaved) {
            throw new IllegalArgumentException("You have already saved this post!");
        }

        SavedPosts savedPost = new SavedPosts();
        savedPost.setUser(loggedUser);
        savedPost.setPost(post);
        savedPostRepo.save(savedPost);

        post.setSaveCount(post.getSaveCount() + 1);
        postRepo.save(post);

        affinityService.updateAffinityOnSave(loggedUser, post);
    }

    // Unsave a Post
    public void unsavePost(Long postId) {
        Users loggedUser = authUtils.getLoggedUser();

        PostsEntity post = postRepo.findActivePost(postId);
        if (post == null) {
            throw new IllegalArgumentException("Post no longer available!");
        }

        Optional<SavedPosts> savedPostOptional = savedPostRepo.findByUserAndPost(loggedUser, post);
        if (savedPostOptional.isPresent()) {
            SavedPosts savedPost = savedPostOptional.get();
            savedPostRepo.delete(savedPost);

            post.setSaveCount(post.getSaveCount() - 1);
            postRepo.save(post);

            affinityService.updateAffinityOnUnsave(loggedUser, post);
        } else {
            throw new IllegalArgumentException("Something went wrong!");
        }
    }

    // View a Post 
    public void viewPost(Long postId) {
        Users loggedUser = authUtils.getLoggedUser();

        PostsEntity post = postRepo.findActivePost(postId);
        if (post == null) {
            throw new IllegalArgumentException("Post no longer available!");
        }

        PostSeen existingSeen = postSeenRepo.findByPostAndUser(post, loggedUser);

        if (existingSeen != null) {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
            LocalDateTime lastViewed = existingSeen.getLastViewedAt();

            boolean tenMinutesPassed = lastViewed.plusMinutes(10).isBefore(now);

            if (tenMinutesPassed) {
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
            PostSeen postSeen = new PostSeen();
            postSeen.setPost(post);
            postSeen.setUser(loggedUser);
            postSeenRepo.save(postSeen);

            post.setViewCount(post.getViewCount() + 1);
            postRepo.save(post);

            affinityService.updateAffinityOnView(loggedUser, post);
        }
    }

    // Comment a Post 
    public void commentPost(Long postId, PostCommentDTO commentDTO) {
        
        Users loggedUser = authUtils.getLoggedUser();

        PostsEntity post = postRepo.findActivePost(postId);
        if (post == null) {
            throw new IllegalArgumentException("Post no longer available!");
        }

        if (!post.getCommentEnabled()) {
            throw new IllegalArgumentException("Comment are disabled!");
        }

        PostComment newComment = new PostComment();
        newComment.setCommentText(commentDTO.getCommentText());
        newComment.setPost(post);
        newComment.setUser(loggedUser);

        if (commentDTO.getParentId() != null) {
            PostComment parentComment = postCommentRepo.findById(commentDTO.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("Comment not found!"));
            newComment.setParentId(parentComment);

            parentComment.setReplyCount(parentComment.getReplyCount() + 1);
            postCommentRepo.save(parentComment);
        }

        postCommentRepo.save(newComment);

        post.setCommentCount(post.getCommentCount() + 1);
        postRepo.save(post);
    }

    // Fetch Comment of a Specific Post
    public List<PostCommentFetchDTO> fetchComment(Long postId, int page) {
        Users loggedUser = authUtils.getLoggedUser();

        PostsEntity post = postRepo.findActivePost(postId);
        if (post == null) {
            throw new IllegalArgumentException("Post no longer available!");
        }

        Pageable pageable = PageRequest.of(page, 15);
        List<PostComment> comments = postCommentRepo.findCommentsByPost(postId, pageable);

        if (comments.isEmpty()) return Collections.emptyList();

        List<PostCommentFetchDTO> dtoList = new ArrayList<>();
        for (PostComment comment : comments) {
            PostCommentFetchDTO dto = new PostCommentFetchDTO();

            dto.setCommentId(comment.getCommentId());
            dto.setCommentText(comment.getCommentText());
            dto.setCreatedAt(comment.getCreatedAt());
            dto.setLikeCount(comment.getLikeCount());
            dto.setReplyCount(comment.getReplyCount());

            if (comment.getParentId() != null) {
                dto.setParentId(comment.getParentId().getCommentId());
            }

            Users user = comment.getUser();
            dto.setUserId(String.valueOf(user.getUserId()));
            dto.setUsername(user.getUsername());
            dto.setFetchVerified(user.isVerifyTag());
            if (user.getUserData() != null) {
                dto.setProfileImage(user.getUserData().getProfilePhoto());
            }

            dtoList.add(dto);
        }

        return dtoList;
    }
}