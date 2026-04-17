package com.loginapp.loginapp.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.loginapp.loginapp.DTO.PostFetchDTO;
import com.loginapp.loginapp.entity.*;
import com.loginapp.loginapp.repository.*;

@Service
public class HomeFeedService {

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private FollowRepo followRepo;

    @Autowired
    private HomeFeedRepo homeFeedRepo;

    @Autowired
    private PostMediaRepo postMediaRepo;

    @Autowired
    private UserAffinityRepo userAffinityRepo;

    public List<PostFetchDTO> getHomeFeed(int page){

        // ✅ 1. Current User
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userUid = Long.parseLong(userIdStr);

        Users user = usersRepo.findByUserId(userUid)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Pageable pageable = PageRequest.of(page, 10);

        // ✅ 2. Following Users
        List<Users> followingUsers = followRepo.findFollowingUsers(user);

        // ✅ 3. Following Posts
        List<PostsEntity> followingPosts = new ArrayList<>();
        if(!followingUsers.isEmpty()){
            followingPosts = homeFeedRepo.getFollowingPosts(followingUsers, pageable);
        }

        // ✅ 4. Interest Based
        List<String> categories = userAffinityRepo.findTopCategories(userUid);

        List<PostsEntity> interestPosts = new ArrayList<>();

        for(String category : categories){
            interestPosts.addAll(
                homeFeedRepo.getPostsByCategory(category, pageable)
            );
        }

        // ✅ 5. Trending + Recent
        List<PostsEntity> trendingPosts = homeFeedRepo.getTrendingPosts(pageable);
        List<PostsEntity> recentPosts = homeFeedRepo.getRecentPosts(pageable);

        // ✅ 6. Merge all
        List<PostsEntity> finalFeed = new ArrayList<>();

        finalFeed.addAll(followingPosts);
        finalFeed.addAll(interestPosts);
        finalFeed.addAll(trendingPosts);
        finalFeed.addAll(recentPosts);

        // ✅ 7. Shuffle (mix feed)
        Collections.shuffle(finalFeed);

        // ✅ 8. Remove duplicates (important 🔥)
        Set<Long> seen = new HashSet<>();
        List<PostsEntity> uniqueFeed = new ArrayList<>();

        for(PostsEntity post : finalFeed){
            if(!seen.contains(post.getPostId())){
                seen.add(post.getPostId());
                uniqueFeed.add(post);
            }
        }

        // ✅ 9. Convert to DTO
        List<PostFetchDTO> dtoList = new ArrayList<>();

        for(PostsEntity post : uniqueFeed){

            PostFetchDTO dto = new PostFetchDTO();

            dto.setFetchPostId(String.valueOf(post.getPostId()));
            dto.setFetchFileName(post.getFileName());
            dto.setFetchPostCaption(post.getPostCaption());
            dto.setFetchPostLocation(post.getPostLocation());
            dto.setFetchUploadAt(post.getUploadAt());

            // 👤 user
            dto.setUserId(String.valueOf(post.getUserpost().getUserId()));
            dto.setUsername(post.getUserpost().getUsername());
            dto.setFullname(post.getUserpost().getFullname());
            dto.setFetchVerified(post.getUserpost().isVerifyTag());

            // ❤️ stats
            dto.setLikeCount(String.valueOf(post.getLikeCount()));
            dto.setCommentCount(String.valueOf(post.getCommentCount()));
            dto.setViewCount(String.valueOf(post.getViewCount()));
            dto.setSaveCount(String.valueOf(post.getSaveCount()));

            // ⚙️ settings
            dto.setCommentEnable(post.getCommentEnabled());
            dto.setShareEnable(post.getShareEnabled());
            dto.setLikeHide(!post.getLikeVisible());

            // 🎥 media
            PostMedia media = postMediaRepo.findPostMedia(post);

            if(media != null){
                dto.setWidth(media.getWidth());
                dto.setHeight(media.getHeight());
                dto.setDuration(media.getDuration());
                dto.setPostType(media.getPostType().name());
            }

            dtoList.add(dto);
        }

        return dtoList;
    }
}