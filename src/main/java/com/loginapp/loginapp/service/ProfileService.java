package com.loginapp.loginapp.service;

import com.loginapp.loginapp.repository.SavedPostRepo;
import org.springframework.data.domain.Pageable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loginapp.loginapp.DTO.LoggedUserResponse;
import com.loginapp.loginapp.DTO.PostFetchDTO;
import com.loginapp.loginapp.DTO.SearchUserResponse;
import com.loginapp.loginapp.Utils.AuthUtils;
import com.loginapp.loginapp.entity.PostMedia;
import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.UserData;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.BlockRepo;
import com.loginapp.loginapp.repository.FollowRepo;
import com.loginapp.loginapp.repository.FollowRequestRepo;
import com.loginapp.loginapp.repository.PostLikeRepo;
import com.loginapp.loginapp.repository.PostRepo;
import com.loginapp.loginapp.repository.SecretCrushRepo;
import com.loginapp.loginapp.repository.SecretCrushRequestRepo;
import com.loginapp.loginapp.repository.UsersRepo;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProfileService {

    @Autowired
    private SavedPostRepo savedPostRepo;

    @Autowired 
    private UsersRepo usersRepo;

    @Autowired
    private FollowRepo followRepo;

    @Autowired
    private PostRepo postRepo;

    @Autowired
    private FollowRequestRepo followRequestRepo;

    @Autowired
    private BlockRepo blockRepo;

    @Autowired
    private SecretCrushRepo secretCrushRepo;

    @Autowired
    private SecretCrushRequestRepo secretCrushRequestRepo;

    @Autowired
    private PostLikeRepo postLikeRepo;

    @Autowired
    private AuthUtils authUtils;

    // Fetch search profile securely using projection
    public SearchUserResponse userProfile(String username) {

        // 1️⃣ Get logged-in user
        Users loggedUser = authUtils.getLoggedUser();

        if(loggedUser.isStatusDeleted()){
            throw new IllegalArgumentException("Something went wrong!");
        }

        // 2️⃣ Get searched user
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

        // 3️⃣ Prepare response
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
                    if (!timelineUser.isStatusDeleted()) {
                        res.setSearchUserTimeline(timelineUser.getFullname());
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
        res.setFollowingCount(followRepo.countByFollower_UserId(user.getUserId()));
        res.setPostCount(postRepo.countByUserpost_UserId(user.getUserId()));

        return res;
    }


    // Set Search User Posts 
    public List<PostFetchDTO> getSearchUserPosts(String username, int page){

        // Current User
        Users loggedUser = authUtils.getLoggedUser();

        // Search User Found
        Users userRes = usersRepo.findByUsername(username)
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

        Set<Long> likedPostIds = postIds.isEmpty() ? Collections.emptySet() :
                postLikeRepo.findLikedPostIdsByUserAndPostIds(loggedUser, postIds);

        Set<Long> savedPostIds = postIds.isEmpty() ? Collections.emptySet() :
                savedPostRepo.findSavedPostIdsByUserAndPostIds(loggedUser, postIds);

        for(PostsEntity post : posts){

            PostFetchDTO dto = new PostFetchDTO();

            dto.setFetchPostId(String.valueOf(post.getPostId()));
            dto.setFetchFileName(post.getFileName());
            dto.setFetchPostLocation(post.getPostLocation());
            dto.setFetchPostCaption(post.getPostCaption());
            dto.setFetchTaggedUsers(post.getTaggedUsers());
            dto.setFetchTimelineUser(String.valueOf(post.getTimelineUser()));
            dto.setFetchUploadAt(post.getUploadAt());
            dto.setFetchVerified(userRes.isVerifyTag());

            // Set Post User Details
            dto.setFullname(post.getUserpost().getFullname());
            dto.setUserId(String.valueOf(post.getUserpost().getUserId()));
            dto.setUsername(post.getUserpost().getUsername());
            if(post.getUserpost().getUserData() != null){
                dto.setProfileImage(post.getUserpost().getUserData().getProfilePhoto());
            }

            PostMedia media = post.getPostMedia();
            if (media != null) {
                dto.setWidth(media.getWidth());
                dto.setHeight(media.getHeight());
                dto.setDuration(media.getDuration());

                if (media.getPostType() != null) {
                    dto.setPostType(media.getPostType().name());
                }
            }

            // Set Counts
            
            dto.setCommentCount(post.getCommentCount());
            dto.setLikeCount(post.getLikeCount());
            dto.setSaveCount(post.getSaveCount());
            dto.setViewCount(post.getViewCount());

            // Set Post Settings

            dto.setCommentEnable(post.getCommentEnabled());
            dto.setLikeHide(post.getLikeVisible());
            dto.setShareEnable(post.getShareEnabled());

            // Set Like Flag
            dto.setLikedByCurrentUser(likedPostIds.contains(post.getPostId()));
            dto.setSavedByCurrentUser(savedPostIds.contains(post.getPostId()));

            postsList.add(dto);
        }

        return postsList;
    }

    // Search User TimeLine Post 
    public List<PostFetchDTO> getSearchUserTimelinePosts(String username, int page){

        // Current User
        Users loggedUser = authUtils.getLoggedUser();

        // Search User Found
        Users userRes = usersRepo.findByUsername(username)
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

        Set<Long> likedPostIds = postIds.isEmpty() ? Collections.emptySet() :
                postLikeRepo.findLikedPostIdsByUserAndPostIds(loggedUser, postIds);

        Set<Long> savedPostIds = postIds.isEmpty() ? Collections.emptySet() :
                savedPostRepo.findSavedPostIdsByUserAndPostIds(loggedUser, postIds);

        for(PostsEntity post : posts){

            PostFetchDTO dto = new PostFetchDTO();

            dto.setFetchPostId(String.valueOf(post.getPostId()));
            dto.setFetchFileName(post.getFileName());
            dto.setFetchPostLocation(post.getPostLocation());
            dto.setFetchPostCaption(post.getPostCaption());
            dto.setFetchTaggedUsers(post.getTaggedUsers());
            dto.setFetchTimelineUser(String.valueOf(post.getTimelineUser()));
            dto.setFetchUploadAt(post.getUploadAt());
            dto.setFetchVerified(userRes.isVerifyTag());

            // Set Post User Details
            dto.setFullname(post.getUserpost().getFullname());
            dto.setUserId(String.valueOf(post.getUserpost().getUserId()));
            dto.setUsername(post.getUserpost().getUsername());
            if(post.getUserpost().getUserData() != null){
                dto.setProfileImage(post.getUserpost().getUserData().getProfilePhoto());
            }

            PostMedia media = post.getPostMedia();
            if (media != null) {
                dto.setWidth(media.getWidth());
                dto.setHeight(media.getHeight());
                dto.setDuration(media.getDuration());
                dto.setPostType(media.getPostType().name());
            }

            dto.setCommentCount(post.getCommentCount());
            dto.setLikeCount(post.getLikeCount());
            dto.setSaveCount(post.getSaveCount());
            dto.setViewCount(post.getViewCount());

            dto.setCommentEnable(post.getCommentEnabled());
            dto.setLikeHide(post.getLikeVisible());
            dto.setShareEnable(post.getShareEnabled());

            // Set Like Flag
            dto.setLikedByCurrentUser(likedPostIds.contains(post.getPostId()));
            dto.setSavedByCurrentUser(savedPostIds.contains(post.getPostId()));

            postsList.add(dto);
        }

        return postsList;
    }

    // Fetch Search User Tagged Posts
    public List<PostFetchDTO> getSearchUserTaggedPosts(String username, int page){

        // Current User
        Users loggedUser = authUtils.getLoggedUser();

        // Search User Found
        Users userRes = usersRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userRes.isStatusDeleted()) {
            throw new IllegalArgumentException("User not found");
        }

        // Check if user blocked the logged-in user
        Boolean isBlocked = blockRepo.existsByBlockerAndBlocked(userRes, loggedUser);
        if (isBlocked) {
            throw new IllegalArgumentException("User not found");
        }

        boolean isFollowingPvt = followRepo.existsByFollower_UserIdAndFollowing_UserId(loggedUser.getUserId(), userRes.getUserId());

        //  self check 
        if(userRes.getUserId().equals(loggedUser.getUserId())){
            isFollowingPvt = true;
        }

        // check if user is private and not following or if logged user blocked the search user then return nothing
        boolean isBlockedByLoggedUser = blockRepo.existsByBlockerAndBlocked(loggedUser, userRes);
        if(isBlockedByLoggedUser || (userRes.isStatusPrivate() && !isFollowingPvt)){
            return Collections.emptyList();
        }

        // check if postowner blocked me or blocked by me them return nothing
        List<Long> blockedByMe = blockRepo.findByBlocker(loggedUser)
            .stream()
            .map(block -> block.getBlocked().getUserId())
            .collect(Collectors.toList());

        List<Long> blockedMe = blockRepo.findByBlocked(loggedUser)
            .stream()
            .map(block -> block.getBlocker().getUserId())
            .collect(Collectors.toList());


        Pageable pageable = PageRequest.of(page, 10);

        List<PostsEntity> posts = postRepo.findTaggedPosts(userRes.getUsername(), pageable);

        List<PostFetchDTO> postsList = new ArrayList<>();

        // Get Liked and Saved Post Ids for the logged-in user
        List<Long> postIds = posts.stream()
        .map(PostsEntity::getPostId)
        .collect(Collectors.toList());

        Set<Long> likedPostIds = postIds.isEmpty() ? Collections.emptySet() :
                postLikeRepo.findLikedPostIdsByUserAndPostIds(loggedUser, postIds);

        Set<Long> savedPostIds = postIds.isEmpty() ? Collections.emptySet() :
                savedPostRepo.findSavedPostIdsByUserAndPostIds(loggedUser, postIds);

        for(PostsEntity post : posts){

            // Check if post owner blocked me or I blocked post owner
            Long postOwnerUserId = post.getUserpost().getUserId();
            if(blockedByMe.contains(postOwnerUserId) || blockedMe.contains(postOwnerUserId)){
                continue;
            }

            PostFetchDTO dto = new PostFetchDTO();

            dto.setFetchPostId(String.valueOf(post.getPostId()));
            dto.setFetchFileName(post.getFileName());
            dto.setFetchPostLocation(post.getPostLocation());
            dto.setFetchPostCaption(post.getPostCaption());
            dto.setFetchTaggedUsers(post.getTaggedUsers());
            dto.setFetchTimelineUser(String.valueOf(post.getTimelineUser()));
            dto.setFetchUploadAt(post.getUploadAt());
            dto.setFetchVerified(userRes.isVerifyTag());

            // Set Post User Details
            dto.setFullname(post.getUserpost().getFullname());
            dto.setUserId(String.valueOf(post.getUserpost().getUserId()));
            dto.setUsername(post.getUserpost().getUsername());
            if(post.getUserpost().getUserData() != null){
                dto.setProfileImage(post.getUserpost().getUserData().getProfilePhoto());
            }

            PostMedia media = post.getPostMedia();
            if (media != null) {
                dto.setWidth(media.getWidth());
                dto.setHeight(media.getHeight());
                dto.setDuration(media.getDuration());
                dto.setPostType(media.getPostType().name());
            }

            dto.setCommentCount(post.getCommentCount());
            dto.setLikeCount(post.getLikeCount());
            dto.setSaveCount(post.getSaveCount());
            dto.setViewCount(post.getViewCount());

            dto.setCommentEnable(post.getCommentEnabled());
            dto.setLikeHide(post.getLikeVisible());
            dto.setShareEnable(post.getShareEnabled());

            // Set Like Flag
            dto.setLikedByCurrentUser(likedPostIds.contains(post.getPostId()));
            dto.setSavedByCurrentUser(savedPostIds.contains(post.getPostId()));

            postsList.add(dto);
        }

        return postsList;
    }

    // Fetch Logged User Data 
    public LoggedUserResponse fetchLoggedData(){

        // 1️⃣ Get logged-in username from JWT
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userUid = Long.parseLong(userIdStr);
        Optional<Users> userOpt = usersRepo.findByUserId(userUid);

        if(userOpt.isEmpty()){
            throw new IllegalArgumentException("Token Expired!"); 
        }

        Users finalUser = userOpt.get();

        // Set Data to DTO elements 
        LoggedUserResponse resData = new LoggedUserResponse();
        resData.setUserUid(finalUser.getUserId());
        resData.setFullName(finalUser.getFullname());
        resData.setUserName(finalUser.getUsername());

        // Get Data from Other Entity which connected to Users
        UserData userData = finalUser.getUserData();
        if(userData != null){
            resData.setProfilePhoto(userData.getProfilePhoto());
            resData.setuBio(userData.getUserBio());
            resData.setuGender(userData.getUserGender());
            resData.setuLink(userData.getUserlink());
            resData.setuLocation(userData.getUserLocation());
            resData.setuBadge(userData.getBadge());
            resData.setuTimeline(userData.getTimeUser());
        }

        return resData;
    }
    
}