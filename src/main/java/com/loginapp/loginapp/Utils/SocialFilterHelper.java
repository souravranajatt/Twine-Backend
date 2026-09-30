package com.loginapp.loginapp.Utils;

import java.util.*;
import org.springframework.stereotype.Component;

import com.loginapp.loginapp.DTO.TaggingResult;
import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.BlockRepo;
import com.loginapp.loginapp.repository.PostLikeRepo;
import com.loginapp.loginapp.repository.SavedPostRepo;
import com.loginapp.loginapp.repository.UsersRepo;

@Component
public class SocialFilterHelper {

    private final BlockRepo blockRepo;
    private final PostLikeRepo postLikeRepo;
    private final SavedPostRepo savedPostRepo;
    private final UsersRepo usersRepo;

    public SocialFilterHelper(BlockRepo blockRepo, PostLikeRepo postLikeRepo, SavedPostRepo savedPostRepo, UsersRepo usersRepo) {
        this.blockRepo = blockRepo;
        this.postLikeRepo = postLikeRepo;
        this.savedPostRepo = savedPostRepo;
        this.usersRepo = usersRepo;
    }

    // Fetch all blocked user IDs (both sides: blocked by me + blocked me) in a single DB query
    public Set<Long> getAllBlockedUserIds(Users user) {
        if (user == null) {
            return Collections.emptySet();
        }
        return blockRepo.findAllBlockedUserIds(user);
    }

    // Batch fetch liked post IDs for current user
    public Set<Long> getLikedPostIds(Users user, List<Long> postIds) {
        if (user == null || postIds == null || postIds.isEmpty()) {
            return Collections.emptySet();
        }
        return postLikeRepo.findLikedPostIdsByUserAndPostIds(user, postIds);
    }

    // Batch fetch saved post IDs for current user
    public Set<Long> getSavedPostIds(Users user, List<Long> postIds) {
        if (user == null || postIds == null || postIds.isEmpty()) {
            return Collections.emptySet();
        }
        return savedPostRepo.findSavedPostIdsByUserAndPostIds(user, postIds);
    }

    // Check if single post is saved by current user
    public boolean isPostSaved(Users user, PostsEntity post) {
        if (user == null || post == null) {
            return false;
        }
        return savedPostRepo.existsByUserAndPost(user, post);
    }

    // Check if single post is liked by current user
    public boolean isPostLiked(Users user, PostsEntity post) {
        if (user == null || post == null) {
            return false;
        }
        return postLikeRepo.existsByPostAndUser(post, user);
    }

    // Filter blocked users and get tagged user details
    public List<TaggingResult> resolveTaggedUsers(List<String> rawTaggedUserIds, Set<Long> blockedIds) {

        if (rawTaggedUserIds == null || rawTaggedUserIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> validIds = new ArrayList<>();
        for (String idStr : rawTaggedUserIds) {
            try {
                Long id = Long.valueOf(idStr);
                if (blockedIds != null && blockedIds.contains(id)) {
                    continue; // Skip blocked user
                }
                validIds.add(idStr);
            } catch (NumberFormatException ignored) {
                // Ignore malformed user id strings
            }
        }

        if (validIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Users> users = usersRepo.findTaggedUsersByIds(validIds);
        List<TaggingResult> results = new ArrayList<>();
        for (Users u : users) {
            TaggingResult dto = new TaggingResult();
            dto.setUserId(u.getUserId().toString());
            dto.setUsername(u.getUsername());
            dto.setVerify(u.isVerifyTag());
            if (u.getUserData() != null) {
                dto.setProfileImage(u.getUserData().getProfilePhoto());
            }
            results.add(dto);
        }
        return results;
    }
}
