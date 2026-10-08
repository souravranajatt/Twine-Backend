package com.loginapp.loginapp.service;

import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loginapp.loginapp.DTO.FollowListFetchDTO;
import com.loginapp.loginapp.DTO.LoggedUserResponse;
import com.loginapp.loginapp.DTO.PostFetchDTO;
import com.loginapp.loginapp.DTO.SearchUserResponse;
import com.loginapp.loginapp.Utils.AuthUtils;
import com.loginapp.loginapp.Utils.PostDTOMapper;
import com.loginapp.loginapp.Utils.SocialFilterHelper;
import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.UserData;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.BlockRepo;
import com.loginapp.loginapp.repository.FollowRepo;
import com.loginapp.loginapp.repository.FollowRequestRepo;
import com.loginapp.loginapp.repository.PostRepo;
import com.loginapp.loginapp.repository.SecretCrushRepo;
import com.loginapp.loginapp.repository.SecretCrushRequestRepo;
import com.loginapp.loginapp.repository.UsersRepo;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class ProfileService {

    private final UsersRepo usersRepo;

    private final FollowRepo followRepo;

    private final PostRepo postRepo;

    private final FollowRequestRepo followRequestRepo;

    private final BlockRepo blockRepo;

    private final SecretCrushRepo secretCrushRepo;

    private final SecretCrushRequestRepo secretCrushRequestRepo;

    private final AuthUtils authUtils;

    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    private final SocialFilterHelper socialFilterHelper;

    private final PostDTOMapper postDTOMapper;

    ProfileService(UsersRepo usersRepo, FollowRepo followRepo, PostRepo postRepo, FollowRequestRepo followRequestRepo, BlockRepo blockRepo, SecretCrushRepo secretCrushRepo, SecretCrushRequestRepo secretCrushRequestRepo, AuthUtils authUtils, RedisService redisService, ObjectMapper objectMapper, SocialFilterHelper socialFilterHelper, PostDTOMapper postDTOMapper) {
        this.usersRepo = usersRepo;
        this.followRepo = followRepo;
        this.postRepo = postRepo;
        this.followRequestRepo = followRequestRepo;
        this.blockRepo = blockRepo;
        this.secretCrushRepo = secretCrushRepo;
        this.secretCrushRequestRepo = secretCrushRequestRepo;
        this.authUtils = authUtils;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
        this.socialFilterHelper = socialFilterHelper;
        this.postDTOMapper = postDTOMapper;
    }



    // ************** Fetch search profile securely ***************
    
    public SearchUserResponse userProfile(String username) {

        // Get logged-in user
        Users loggedUser = authUtils.getLoggedUser();

        // Redis Cache Key: Profile depends on both the searched user and the logged-in viewer
        String cacheKey = "PROFILE_" + username.toLowerCase() + "_VIEWER_" + loggedUser.getUserId();

        // try {
        //     String cachedData = redisService.getValue(cacheKey);
        //     if (cachedData != null) {
        //         return objectMapper.readValue(cachedData, SearchUserResponse.class);
        //     }
        // } catch (Exception e) {
        //     System.out.println("Redis Cache Read Error: " + e.getMessage());
        //     // Fallback to database on cache read error
        // }

        // Get searched user
        Users user = usersRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check deleted
        if (user.isStatusDeleted()) {
            throw new IllegalArgumentException("User not found");
        }

        // Check if searched user blocked logged-in user
        if (blockRepo.existsByBlockerAndBlocked(user, loggedUser)) {
            throw new IllegalArgumentException("User not found");
        }

        // Prepare response
        SearchUserResponse res = new SearchUserResponse();

        // ================= BASIC USER INFO =================
        res.setSearchUserId(String.valueOf(user.getUserId()));
        res.setSearchUsername(user.getUsername());
        res.setSearchFullname(user.getFullname());
        res.setSearchVerified(user.isVerifyTag());
        res.setSearchCreatedAt(user.getCreatedAt());

        // ================= USER DATA =================
        if (user.getUserData() != null) {
            UserData data = user.getUserData();
            res.setSearchProfilePhoto(data.getProfilePhoto());
            res.setSearchUserBio(data.getUserBio());
            res.setSearchUserLocation(data.getUserLocation());
            res.setSearchUserLink(data.getUserlink());
            res.setSearchBadge(data.getBadge());
            res.setSearchUserGender(data.getUserGender());

            if (data.getTimeUser() != null) {
                usersRepo.findByUserId(data.getTimeUser()).ifPresent(timelineUser -> {
                    if (!timelineUser.isStatusDeleted() && !timelineUser.isStatusSuspend()) {
                        boolean blockedbyme = blockRepo.existsByBlockerAndBlocked(loggedUser, timelineUser);
                        boolean blockedme = blockRepo.existsByBlockerAndBlocked(timelineUser, loggedUser);
                        if(blockedbyme || blockedme){
                            res.setSearchUserTimeline(null);
                        }else{
                            res.setSearchUserTimeline(timelineUser.getUsername() != null ? timelineUser.getUsername() : timelineUser.getFullname());
                        }
                    }
                    
                });
            }
        }

        // ================= PRIVATE / BLOCK LOGIC =================
        boolean isPrivate = user.isStatusPrivate();
        boolean isFollowing = followRepo.existsByFollower_UserIdAndFollowing_UserId(loggedUser.getUserId(), user.getUserId());
        boolean isBlockedByLoggedUser = blockRepo.existsByBlockerAndBlocked(loggedUser, user);

        res.setSearchPrivate(isPrivate);
        res.setSearchPrivateShow((!isPrivate || isFollowing) && !isBlockedByLoggedUser);
        res.setBlockedStatus(isBlockedByLoggedUser);

        // ================= SELF vs OTHER =================
        boolean isSelf = user.getUserId().equals(loggedUser.getUserId());
        res.setSearchLoggedUser(isSelf);

        if (isSelf) {
            res.setSearchPrivate(false);
            res.setSearchPrivateShow(true);
            res.setFollowingStatus(false);
            res.setFollowerStatus(false);
            res.setFollowReqStatus(false);
            res.setFollowReqOptStatus(false);
            res.setCrushStatus(false);
            res.setCrushSentStatus(false);
            res.setBlockedStatus(false);
        } else {
            res.setFollowingStatus(isFollowing);
            res.setFollowerStatus(followRepo.existsByFollower_UserIdAndFollowing_UserId(user.getUserId(), loggedUser.getUserId()));
            res.setFollowReqStatus(followRequestRepo.existsBySenderIdAndReceiverId(loggedUser, user));
            res.setFollowReqOptStatus(followRequestRepo.existsBySenderIdAndReceiverId(user, loggedUser));

            // ================= CRUSH STATUS =================
            boolean isCrushMatched = secretCrushRepo.existsByUserOneAndUserTwo(loggedUser, user)
                                || secretCrushRepo.existsByUserOneAndUserTwo(user, loggedUser);
            res.setCrushStatus(isCrushMatched);
            res.setCrushSentStatus(secretCrushRequestRepo.existsBySenderIdAndAnonymousId(loggedUser, user));
        }

        // ================= COUNTS =================
        res.setFollowersCount(followRepo.countByFollowing_UserId(user.getUserId()));
        res.setPostCount(postRepo.countByUserpost_UserId(user.getUserId()));


        // Redis Service Call 
        try {
            // Save to Redis cache for 5 minutes (300 seconds)
            String jsonRes = objectMapper.writeValueAsString(res);
            redisService.setValueWithExpiry(cacheKey, jsonRes, 300);
        } catch (Exception e) {
            System.out.println("Redis Cache Write Error: " + e.getMessage());
            // Fallback: ignore cache write error
        }

        return res;
    }


    // Set Search User Posts 
    public List<PostFetchDTO> getSearchUserPosts(String username, int page){

        // Current User
        Users loggedUser = authUtils.getLoggedUser();

        // Search User Found
        Users userRes = usersRepo.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if user blocked the logged-in user
        Boolean isBlocked = blockRepo.existsByBlockerAndBlocked(userRes, loggedUser);
        if (isBlocked) {
            throw new IllegalArgumentException("User not found");
        }

        // Check if user is deactivate or deleted 
        if (userRes.isStatusDeleted()) {
            throw new IllegalArgumentException("User not found");
        }

        boolean isFollowingPvt = followRepo.existsByFollower_UserIdAndFollowing_UserId(loggedUser.getUserId(), userRes.getUserId());

        // Self check
        if(userRes.getUserId().equals(loggedUser.getUserId())){
            isFollowingPvt = true;
        }

        boolean isBlockedByLoggedUser = blockRepo.existsByBlockerAndBlocked(loggedUser, userRes);
        if(isBlockedByLoggedUser || (userRes.isStatusPrivate() && !isFollowingPvt)){
            return Collections.emptyList();
        }

        Pageable pageable = PageRequest.of(page, 10);

        List<PostsEntity> posts = postRepo.findUserPosts(userRes, pageable);

        List<PostFetchDTO> postsList = new ArrayList<>();

        // Get Liked and Saved Post Ids for the logged-in user
        List<Long> postIds = posts.stream()
                .map(PostsEntity::getPostId)
                .collect(Collectors.toList());

        Set<Long> likedPostIds = socialFilterHelper.getLikedPostIds(loggedUser, postIds);

        Set<Long> savedPostIds = socialFilterHelper.getSavedPostIds(loggedUser, postIds);

        // Batch Fetch Blocked User IDs
        Set<Long> blockedIds = socialFilterHelper.getAllBlockedUserIds(loggedUser);

        for(PostsEntity post : posts){
            postsList.add(postDTOMapper.toDTO(post, loggedUser, blockedIds, likedPostIds, savedPostIds));
        }

        return postsList;
    }

    // Search User TimeLine Post 
    public List<PostFetchDTO> getSearchUserTimelinePosts(String username, int page){

        // Current User
        Users loggedUser = authUtils.getLoggedUser();

        // Search User Found
        Users userRes = usersRepo.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userRes.isStatusDeleted()) {
            throw new IllegalArgumentException("User not found");
        }

        if(userRes.getUserData() == null || userRes.getUserData().getTimeUser() == null){
            return Collections.emptyList();
        }

        // Check if user blocked the logged-in user
        Boolean isBlocked = blockRepo.existsByBlockerAndBlocked(userRes, loggedUser);
        if (isBlocked) {
            throw new IllegalArgumentException("User not found");
        }

        boolean isFollowingPvt = followRepo.existsByFollower_UserIdAndFollowing_UserId(loggedUser.getUserId(), userRes.getUserId());

        //  Self check 
        if(userRes.getUserId().equals(loggedUser.getUserId())){
            isFollowingPvt = true;
        }

        boolean isBlockedByLoggedUser = blockRepo.existsByBlockerAndBlocked(loggedUser, userRes);
        if(isBlockedByLoggedUser || (userRes.isStatusPrivate() && !isFollowingPvt)){
            return Collections.emptyList();
        }

        // Handle Timeline User with Logged User 
        Users timelineUser = usersRepo.findByUserId(userRes.getUserData().getTimeUser())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (timelineUser.isStatusDeleted()) {
            return Collections.emptyList();
        }

        Boolean isTimelineBlocked = blockRepo.existsByBlockerAndBlocked(timelineUser, loggedUser);
        Boolean isLoggedBlockedTimeline = blockRepo.existsByBlockerAndBlocked(loggedUser, timelineUser);
        if(isTimelineBlocked || isLoggedBlockedTimeline) return Collections.emptyList();

        Pageable pageable = PageRequest.of(page, 10);
        
        List<PostsEntity> posts = postRepo.findTimelinePosts(timelineUser.getUserId(), userRes.getUserId(), pageable);

        List<PostFetchDTO> postsList = new ArrayList<>();

        // Get Liked and Saved Post Ids for the logged-in user
        List<Long> postIds = posts.stream()
        .map(PostsEntity::getPostId)
        .collect(Collectors.toList());

        Set<Long> likedPostIds = socialFilterHelper.getLikedPostIds(loggedUser, postIds);

        Set<Long> savedPostIds = socialFilterHelper.getSavedPostIds(loggedUser, postIds);

        // Batch Fetch Blocked User IDs
        Set<Long> blockedIds = socialFilterHelper.getAllBlockedUserIds(loggedUser);
        
        for(PostsEntity post : posts){
            postsList.add(postDTOMapper.toDTO(post, loggedUser, blockedIds, likedPostIds, savedPostIds));
        }

        return postsList;
    }

    // Fetch Search User Tagged Posts
    public List<PostFetchDTO> getSearchUserTaggedPosts(String username, int page){

        // Current User
        Users loggedUser = authUtils.getLoggedUser();

        // Search User Found
        Users userRes = usersRepo.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userRes.isStatusDeleted()) {
            throw new IllegalArgumentException("User not found");
        }

        boolean isFollowingPvt = followRepo.existsByFollower_UserIdAndFollowing_UserId(loggedUser.getUserId(), userRes.getUserId());

        // Check if user blocked the logged-in user
        Boolean isBlocked = blockRepo.existsByBlockerAndBlocked(userRes, loggedUser);
        if (isBlocked) {
            throw new IllegalArgumentException("User not found");
        }

        // Self check
        if(userRes.getUserId().equals(loggedUser.getUserId())){
            isFollowingPvt = true;
        }

        // Check if user is private and not following or if logged user blocked the search user then return nothing
        boolean isBlockedByLoggedUser = blockRepo.existsByBlockerAndBlocked(loggedUser, userRes);
        if(isBlockedByLoggedUser || (userRes.isStatusPrivate() && !isFollowingPvt)){
            return Collections.emptyList();
        }

        // Batch Fetch Blocked User IDs
        Set<Long> blockedIds = socialFilterHelper.getAllBlockedUserIds(loggedUser);


        Pageable pageable = PageRequest.of(page, 10);

        List<PostsEntity> posts = postRepo.findTaggedPosts(String.valueOf(userRes.getUserId()), pageable);

        List<PostFetchDTO> postsList = new ArrayList<>();

        // Get Liked and Saved Post Ids for the logged-in user
        List<Long> postIds = posts.stream()
        .map(PostsEntity::getPostId)
        .collect(Collectors.toList());

        List<Long> postOwnerIds = posts.stream()
        .filter(post -> post.getUserpost() != null && post.getUserpost().isStatusPrivate())
        .map(post -> post.getUserpost().getUserId())
        .distinct()
        .collect(Collectors.toList());

        Set<Long> followedUserIds = postOwnerIds.isEmpty() ? Collections.emptySet()
                : followRepo.findFollowingIds(loggedUser, postOwnerIds);

        Set<Long> likedPostIds = socialFilterHelper.getLikedPostIds(loggedUser, postIds);

        Set<Long> savedPostIds = socialFilterHelper.getSavedPostIds(loggedUser, postIds);

        for(PostsEntity post : posts){

            // Check if post owner blocked me or I blocked post owner
            Long postOwnerUserId = post.getUserpost().getUserId();
            if(blockedIds.contains(postOwnerUserId)){
                continue;
            }

            // Hide private users' posts unless the logged-in user is the owner or follows them
            if (post.getUserpost().isStatusPrivate() && !postOwnerUserId.equals(loggedUser.getUserId()) && !followedUserIds.contains(postOwnerUserId)) {
                continue;
            }

            postsList.add(postDTOMapper.toDTO(post, loggedUser, blockedIds, likedPostIds, savedPostIds));
        }

        return postsList;
    }



    
    // Fetch Follower List with pagination
    public List<FollowListFetchDTO> followerListFetch(Long targetUserId, int page) {

        // Get logged-in user
        Users userOne = authUtils.getLoggedUser();

        // Target user
        Users userTwo = usersRepo.findByUserId(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found!"));

        // Check if target user is soft deleted/deactivated
        if (userTwo.isStatusDeleted()) {
            throw new IllegalArgumentException("User is not available!");
        }

        // Check block relationship
        boolean isBlocked = blockRepo.existsByBlockerAndBlocked(userOne, userTwo)
                || blockRepo.existsByBlockerAndBlocked(userTwo, userOne);
        if (isBlocked) {
            throw new IllegalArgumentException("Invalid Action!");
        }

        // Privacy check
        if (userTwo.isStatusPrivate() && !userOne.getUserId().equals(userTwo.getUserId())) {
            boolean isFollowing = followRepo.existsByFollowerAndFollowing(userOne, userTwo);
            if (!isFollowing && !userOne.getUserId().equals(targetUserId)) {
                throw new IllegalArgumentException("This account is private!");
            }
        }

        // Fetch followers
        Pageable pageable = PageRequest.of(page, 15);
        List<Users> followers = followRepo.findFollowerUsers(userTwo, pageable);
        if (followers.isEmpty()) return Collections.emptyList();

        // Block IDs batch fetch
        Set<Long> blockedIds = socialFilterHelper.getAllBlockedUserIds(userOne);

        // Filter blocked users
        List<Users> filteredFollowers = followers.stream()
                .filter(f -> !blockedIds.contains(f.getUserId()))
                .toList();

        if (filteredFollowers.isEmpty()) return Collections.emptyList();

        // Batch fetch follow relations
        List<Long> followerIds = filteredFollowers.stream()
                .map(Users::getUserId)
                .toList();

        Set<Long> theyFollowMe = followRepo.findFollowerIds(userOne, followerIds);
        Set<Long> iFollowThem = followRepo.findFollowingIds(userOne, followerIds);

        // DTO Convert
        List<FollowListFetchDTO> fetchList = new ArrayList<>();
        for (Users follower : filteredFollowers) {
            FollowListFetchDTO dto = new FollowListFetchDTO();
            dto.setUserId(String.valueOf(follower.getUserId()));
            dto.setUsername(follower.getUsername());
            dto.setVerify(follower.isVerifyTag());
            if (follower.getUserData() != null && follower.getUserData().getProfilePhoto() != null) {
                dto.setProfilePicture(follower.getUserData().getProfilePhoto());
            }
            dto.setFollowsYou(theyFollowMe.contains(follower.getUserId()));
            dto.setFollowedByMe(iFollowThem.contains(follower.getUserId()));
            dto.setIsMe(follower.getUserId().equals(userOne.getUserId()));
            fetchList.add(dto);
        }

        return fetchList;
    }





    // Fetch Logged User Data 
    public LoggedUserResponse fetchLoggedData(){

        Users finalUser = authUtils.getLoggedUser();

        // Set Data to DTO elements 
        LoggedUserResponse resData = new LoggedUserResponse();
        resData.setUserUid(String.valueOf(finalUser.getUserId()));
        resData.setFullName(finalUser.getFullname());
        resData.setUserName(finalUser.getUsername());
        resData.setVerify(finalUser.isVerifyTag());

        // Get Data from Other Entity which connected to Users
        UserData userData = finalUser.getUserData();
        if(userData != null){
            resData.setProfilePhoto(userData.getProfilePhoto());
            resData.setuBio(userData.getUserBio());
            resData.setuGender(userData.getUserGender());
            resData.setuLink(userData.getUserlink());
            resData.setuLocation(userData.getUserLocation());
            resData.setuBadge(userData.getBadge());
            if(userData.getTimeUser() != null){
                Optional<Users> timelineUserOpt = usersRepo.findByUserId(userData.getTimeUser());
                if(timelineUserOpt.isPresent()){
                    Users timelineUser = timelineUserOpt.get();
                    if(!timelineUser.isStatusDeleted() && !timelineUser.isStatusSuspend()){
                        resData.setuTimeline(true);
                    }else{
                        resData.setuTimeline(false);
                    }
                }else{
                    resData.setuTimeline(false);
                }
            }else{
                resData.setuTimeline(false);
            }
            
        }

        return resData;
    }
    
}