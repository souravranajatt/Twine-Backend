package com.loginapp.loginapp.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loginapp.loginapp.DTO.PostFetchDTO;
import com.loginapp.loginapp.Utils.AuthUtils;
import com.loginapp.loginapp.entity.*;
import com.loginapp.loginapp.repository.*;

@Service
@Transactional
public class HomeFeedService {

    @Autowired
    private FollowRepo followRepo;

    @Autowired
    private HomeFeedRepo homeFeedRepo;

    @Autowired
    private UserAffinityRepo userAffinityRepo;

    @Autowired
    private BlockRepo blockRepo;

    @Autowired
    private PostLikeRepo postLikeRepo;

    @Autowired
    private SavedPostRepo savedPostRepo;

    @Autowired
    private AuthUtils authUtils;

    public List<PostFetchDTO> getHomeFeed(int page){

        // 1. Current User
        Users user = authUtils.getLoggedUser();
        

        if(user.isStatusDeleted()){
            throw new IllegalArgumentException("User not found");
        }

        Pageable pageable = PageRequest.of(page, 10);
        Pageable categoryPageable = PageRequest.of(0, 5);

        // 2. Blocked IDs fetch 
        List<Users> blockedUsers = blockRepo.findBlockedUsers(user);
        List<Users> blockedByUsers = blockRepo.findBlockedByUsers(user);

        Set<Long> blockedIds = new HashSet<>();
        blockedUsers.forEach(u -> blockedIds.add(u.getUserId()));
        blockedByUsers.forEach(u -> blockedIds.add(u.getUserId()));

        // 3. Following Users
        List<Users> followingUsers = followRepo.findFollowingUsers(user);

        List<PostsEntity> followingPosts = new ArrayList<>();
        if(!followingUsers.isEmpty()){
            followingPosts = homeFeedRepo.getFollowingPosts(followingUsers, pageable);
        }

        // 4. Interest Based
        List<String> categories = userAffinityRepo.findTopCategories(user.getUserId());

        List<PostsEntity> interestPosts = new ArrayList<>();
        for(String category : categories){
            interestPosts.addAll(
                homeFeedRepo.getPostsByCategory(category, categoryPageable)
            );
        }

        // 5. Trending + Recent
        List<PostsEntity> trendingPosts = homeFeedRepo.getTrendingPosts(pageable);
        List<PostsEntity> recentPosts = homeFeedRepo.getRecentPosts(pageable);

        // 6. Merge all
        List<PostsEntity> finalFeed = new ArrayList<>();

        finalFeed.addAll(followingPosts);
        finalFeed.addAll(interestPosts);
        finalFeed.addAll(trendingPosts);
        finalFeed.addAll(recentPosts);

        // 7. Shuffle (mix feed)
        Collections.shuffle(finalFeed);

        // 8. Filter out blocked content
        finalFeed.removeIf(post ->
            blockedIds.contains(post.getUserpost().getUserId()) ||
            post.getUserpost().getUserId().equals(user.getUserId()));

        // 9. Remove duplicates (important 🔥)
        Set<Long> seen = new HashSet<>();
        List<PostsEntity> uniqueFeed = new ArrayList<>();

        for(PostsEntity post : finalFeed){
            if(!seen.contains(post.getPostId())){
                seen.add(post.getPostId());
                uniqueFeed.add(post);
            }
        }

        // Like and Saved post ids for logged user 
        List<Long> postIds = new ArrayList<>();
        for(PostsEntity post : uniqueFeed){
            postIds.add(post.getPostId());
        }

        Set<Long> likedPostIds = postIds.isEmpty() ? Collections.emptySet() :
                postLikeRepo.findLikedPostIdsByUserAndPostIds(user, postIds);

        Set<Long> savedPostIds = postIds.isEmpty() ? Collections.emptySet() :
                savedPostRepo.findSavedPostIdsByUserAndPostIds(user, postIds);

        // 10. Convert to DTO
        List<PostFetchDTO> dtoList = new ArrayList<>();

        for(PostsEntity post : uniqueFeed){

            PostFetchDTO dto = new PostFetchDTO();

            dto.setFetchPostId(String.valueOf(post.getPostId()));
            dto.setFetchFileName(post.getFileName());
            dto.setFetchPostCaption(post.getPostCaption());
            dto.setFetchPostLocation(post.getPostLocation());
            dto.setFetchUploadAt(post.getUploadAt());

            // user details
            dto.setUserId(String.valueOf(post.getUserpost().getUserId()));
            dto.setUsername(post.getUserpost().getUsername());
            dto.setFullname(post.getUserpost().getFullname());
            if(post.getUserpost().getUserData() != null){
                dto.setProfileImage(post.getUserpost().getUserData().getProfilePhoto());
            }
            dto.setFetchVerified(post.getUserpost().isVerifyTag());

            // stats record
            dto.setLikeCount(post.getLikeCount());
            dto.setCommentCount(post.getCommentCount());
            dto.setViewCount(post.getViewCount());
            dto.setSaveCount(post.getSaveCount());

            // setting data
            dto.setCommentEnable(post.getCommentEnabled());
            dto.setShareEnable(post.getShareEnabled());
            dto.setLikeHide(!post.getLikeVisible());

            // media data
            PostMedia media = post.getPostMedia();

            if(media != null){
                dto.setWidth(media.getWidth());
                dto.setHeight(media.getHeight());
                dto.setDuration(media.getDuration());
                dto.setPostType(media.getPostType().name());
            }

            // Status for Like and Saved
            dto.setLikedByCurrentUser(likedPostIds.contains(post.getPostId()));
            dto.setSavedByCurrentUser(savedPostIds.contains(post.getPostId()));

            dtoList.add(dto);
        }

        return dtoList;
    }
}