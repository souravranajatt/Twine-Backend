package com.loginapp.loginapp.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loginapp.loginapp.DTO.PostFetchDTO;
import com.loginapp.loginapp.DTO.TaggingResult;
import com.loginapp.loginapp.Utils.AuthUtils;
import com.loginapp.loginapp.entity.PostMedia;
import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.UserCategoryAffinity;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.*;

@Service
@Transactional
public class HomeFeedService {

    // Inject Other Files thorugh Constructor

    private final FollowRepo followRepo;
    private final HomeFeedRepo homeFeedRepo;
    private final UserAffinityRepo userAffinityRepo;
    private final BlockRepo blockRepo;
    private final PostLikeRepo postLikeRepo;
    private final SavedPostRepo savedPostRepo;
    private final PostSeenRepo postSeenRepo;
    private final AuthUtils authUtils;
    private final UsersRepo usersRepo;

    HomeFeedService(
        FollowRepo followRepo,
        AuthUtils authUtils,
        BlockRepo blockRepo,
        UserAffinityRepo userAffinityRepo,
        PostLikeRepo postLikeRepo,
        HomeFeedRepo homeFeedRepo,
        SavedPostRepo savedPostRepo,
        PostSeenRepo postSeenRepo,
        UsersRepo usersRepo
    ){
        this.followRepo = followRepo;
        this.authUtils = authUtils;
        this.blockRepo = blockRepo;
        this.userAffinityRepo = userAffinityRepo;
        this.postLikeRepo = postLikeRepo;
        this.homeFeedRepo = homeFeedRepo;
        this.savedPostRepo = savedPostRepo;
        this.postSeenRepo = postSeenRepo;
        this.usersRepo = usersRepo;
    }

    public List<PostFetchDTO> getHomeFeed(int page) {

        // 1. Get Logged User Info
        Users user = authUtils.getLoggedUser();
        if(user.isStatusDeleted()){
            throw new IllegalArgumentException("User not found");
        }

        // 2. Get all Blocked and Blocker IDs
        Set<Long> blockedIds = new HashSet<>();
        blockRepo.findBlockedUsers(user).forEach(u -> blockedIds.add(u.getUserId()));
        blockRepo.findBlockedByUsers(user).forEach(u -> blockedIds.add(u.getUserId()));

        // 3. Get all viewed Post IDs
        Set<Long> seenPostIds = postSeenRepo.findSeenPostIdsByUser(user);
        Set<Long> safeSeenIds = seenPostIds.isEmpty() ? Set.of(-1L) : seenPostIds;

        // 4. Set Date For Filter in DB Query
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        LocalDateTime twoMonthsAgo = now.minusMonths(2);
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime oneDayAgo = now.minusHours(24);

        // 5. Get Following Users Posts
        List<Users> followingUsers = followRepo.findFollowingUsers(user);
        List<PostsEntity> followingPosts = new ArrayList<>();
        if(!followingUsers.isEmpty()){
            followingPosts = homeFeedRepo.getFollowingPosts(
                followingUsers,
                safeSeenIds,
                PageRequest.of(page, 10)
            );
        }

        // 6. Interest Based Posts Fetching
        List<UserCategoryAffinity> affinities = userAffinityRepo
            .findTopAffinitiesWithScore(user.getUserId());

        List<PostsEntity> interestPosts = new ArrayList<>();
        if(!affinities.isEmpty()){

            float totalScore = 0f;
            for(UserCategoryAffinity aff : affinities){
                totalScore += aff.getAffinityScore();
            }

            int totalInterestPosts = 15;

            for(UserCategoryAffinity aff : affinities){
                float ratio = aff.getAffinityScore() / totalScore;
                int postsForCategory = Math.max(1, Math.round(ratio * totalInterestPosts));

                interestPosts.addAll(
                    homeFeedRepo.getPostsByCategory(
                        aff.getCategory(),
                        twoMonthsAgo,
                        oneDayAgo,
                        safeSeenIds,
                        PageRequest.of(0, postsForCategory)
                    )
                );
            }
        }

        // 7. Trending Posts Fetching
        List<PostsEntity> trendingPosts = homeFeedRepo.getTrendingPosts(
            sevenDaysAgo,
            oneDayAgo,
            PageRequest.of(0, 10)
        );

        // 8. Merge all posts with weight
        List<PostsEntity> finalFeed = new ArrayList<>();
        finalFeed.addAll(followingPosts.stream().limit(10).toList()); // 40%
        finalFeed.addAll(interestPosts.stream().limit(8).toList());   // 30%
        finalFeed.addAll(trendingPosts.stream().limit(5).toList());   // 20%

        // 9. If Feed Posts is less than Add Random High Reaching Posts
        if(finalFeed.size() < 10){
            List<PostsEntity> fallbackPosts = homeFeedRepo.getFallbackPosts(
                safeSeenIds,
                PageRequest.of(0, 20)
            );
            finalFeed.addAll(fallbackPosts);
        }

        // 10. Mix all Posts 
        Collections.shuffle(finalFeed);

        // 11. Checking blocker and blocked User posts and myself too
        finalFeed.removeIf(post ->
            blockedIds.contains(post.getUserpost().getUserId()) ||
            post.getUserpost().getUserId().equals(user.getUserId())
        );

        // 12. Remove Duplicate
        Set<Long> seenInFeed = new HashSet<>();
        List<PostsEntity> uniqueFeed = new ArrayList<>();
        for(PostsEntity post : finalFeed){
            if(seenInFeed.add(post.getPostId())){
                uniqueFeed.add(post);
            }
        }

        // 13. Get all  Like/Saved Posts Ids
        List<Long> postIds = uniqueFeed.stream()
            .map(PostsEntity::getPostId)
            .toList();

        Set<Long> likedPostIds = postIds.isEmpty() ? Collections.emptySet() :
            postLikeRepo.findLikedPostIdsByUserAndPostIds(user, postIds);

        Set<Long> savedPostIds = postIds.isEmpty() ? Collections.emptySet() :
            savedPostRepo.findSavedPostIdsByUserAndPostIds(user, postIds);

        // 14. DTO Convert
        List<PostFetchDTO> dtoList = new ArrayList<>();
        for(PostsEntity post : uniqueFeed){

            PostFetchDTO dto = new PostFetchDTO();

            dto.setFetchPostId(String.valueOf(post.getPostId()));
            dto.setFetchFileName(post.getFileName());
            dto.setFetchPostCaption(post.getPostCaption());
            dto.setFetchPostLocation(post.getPostLocation());
            dto.setFetchUploadAt(post.getUploadAt());

            // User details
            dto.setUserId(String.valueOf(post.getUserpost().getUserId()));
            dto.setUsername(post.getUserpost().getUsername());
            dto.setFullname(post.getUserpost().getFullname());
            if(post.getUserpost().getUserData() != null){
                dto.setProfileImage(post.getUserpost().getUserData().getProfilePhoto());
            }
            dto.setFetchVerified(post.getUserpost().isVerifyTag());

            // Tagged Users
            List<String> taggedUsersId = new ArrayList<>();
            if (post.getTaggedUsers() != null && !post.getTaggedUsers().isEmpty()) {
                for (String taggedUser : post.getTaggedUsers()) {
                    Long taggedUserId;
                    try {
                        taggedUserId = Long.valueOf(taggedUser);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    if (blockedIds.contains(taggedUserId)) {
                        continue;
                    }
                    taggedUsersId.add(taggedUser);
                }
            }
            List<Users> taggedUsers = usersRepo.findTaggedUsersByIds(taggedUsersId);
            List<TaggingResult> taggingResults = new ArrayList<>();
            for (Users u : taggedUsers) {
                TaggingResult dtoTag = new TaggingResult();
                dtoTag.setUserId(u.getUserId().toString());
                dtoTag.setUsername(u.getUsername());
                dtoTag.setVerify(u.isVerifyTag());
                if (u.getUserData() != null) {
                    dtoTag.setProfileImage(u.getUserData().getProfilePhoto());
                }
                taggingResults.add(dtoTag);
            }
            dto.setFetchTaggedUsers(taggingResults);

            // Stats
            dto.setLikeCount(post.getLikeCount());
            dto.setCommentCount(post.getCommentCount());
            dto.setViewCount(post.getViewCount());
            dto.setSaveCount(post.getSaveCount());

            // Settings
            dto.setCommentEnable(post.getCommentEnabled());
            dto.setShareEnable(post.getShareEnabled());
            dto.setLikeVisible(post.getLikeVisible());

            // Posts Media Data
            PostMedia media = post.getPostMedia();
            if(media != null){
                dto.setWidth(media.getWidth());
                dto.setHeight(media.getHeight());
                dto.setDuration(media.getDuration());
                dto.setPostType(media.getPostType().name());
            }

            // Like/Save status
            dto.setLikedByCurrentUser(likedPostIds.contains(post.getPostId()));
            dto.setSavedByCurrentUser(savedPostIds.contains(post.getPostId()));
            dto.setOwnPost(post.getUserpost().getUserId().equals(user.getUserId()));

            dtoList.add(dto);
        }

        return dtoList;
    }
}