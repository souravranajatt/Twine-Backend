package com.loginapp.loginapp.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loginapp.loginapp.DTO.PostFetchDTO;
import com.loginapp.loginapp.Utils.AuthUtils;
import com.loginapp.loginapp.Utils.PostDTOMapper;
import com.loginapp.loginapp.Utils.SocialFilterHelper;
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
    private final PostSeenRepo postSeenRepo;
    private final AuthUtils authUtils;
    private final SocialFilterHelper socialFilterHelper;
    private final PostDTOMapper postDTOMapper;

    HomeFeedService(
        FollowRepo followRepo,
        AuthUtils authUtils,
        UserAffinityRepo userAffinityRepo,
        HomeFeedRepo homeFeedRepo,
        PostSeenRepo postSeenRepo,
        SocialFilterHelper socialFilterHelper,
        PostDTOMapper postDTOMapper
    ){
        this.followRepo = followRepo;
        this.authUtils = authUtils;
        this.userAffinityRepo = userAffinityRepo;
        this.homeFeedRepo = homeFeedRepo;
        this.postSeenRepo = postSeenRepo;
        this.socialFilterHelper = socialFilterHelper;
        this.postDTOMapper = postDTOMapper;
    }

    public List<PostFetchDTO> getHomeFeed(int page) {

        // 1. Get Logged User Info
        Users user = authUtils.getLoggedUser();
        if(user.isStatusDeleted()){
            throw new IllegalArgumentException("User not found");
        }

        // 2. Get all Blocked and Blocker IDs
        Set<Long> blockedIds = socialFilterHelper.getAllBlockedUserIds(user);

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

        Set<Long> likedPostIds = socialFilterHelper.getLikedPostIds(user, postIds);

        Set<Long> savedPostIds = socialFilterHelper.getSavedPostIds(user, postIds);

        // 14. DTO Convert using centralized mapper
        List<PostFetchDTO> dtoList = new ArrayList<>();
        for(PostsEntity post : uniqueFeed){
            dtoList.add(postDTOMapper.toDTO(post, user, blockedIds, likedPostIds, savedPostIds));
        }

        return dtoList;
    }
}