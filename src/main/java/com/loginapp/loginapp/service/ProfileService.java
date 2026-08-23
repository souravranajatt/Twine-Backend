package com.loginapp.loginapp.service;

import com.loginapp.loginapp.repository.SavedPostRepo;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loginapp.loginapp.DTO.FollowListFetchDTO;
import com.loginapp.loginapp.DTO.LoggedUserResponse;
import com.loginapp.loginapp.DTO.PostFetchDTO;
import com.loginapp.loginapp.DTO.SearchUserResponse;
import com.loginapp.loginapp.DTO.TaggingResult;
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

    private final SavedPostRepo savedPostRepo;

    private final UsersRepo usersRepo;

    private final FollowRepo followRepo;

    private final PostRepo postRepo;

    private final FollowRequestRepo followRequestRepo;

    private final BlockRepo blockRepo;

    private final SecretCrushRepo secretCrushRepo;

    private final SecretCrushRequestRepo secretCrushRequestRepo;

    private final PostLikeRepo postLikeRepo;

    private final AuthUtils authUtils;

    ProfileService(SavedPostRepo savedPostRepo, UsersRepo usersRepo, FollowRepo followRepo, PostRepo postRepo, FollowRequestRepo followRequestRepo, BlockRepo blockRepo, SecretCrushRepo secretCrushRepo, SecretCrushRequestRepo secretCrushRequestRepo, PostLikeRepo postLikeRepo, AuthUtils authUtils) {
        this.savedPostRepo = savedPostRepo;
        this.usersRepo = usersRepo;
        this.followRepo = followRepo;
        this.postRepo = postRepo;
        this.followRequestRepo = followRequestRepo;
        this.blockRepo = blockRepo;
        this.secretCrushRepo = secretCrushRepo;
        this.secretCrushRequestRepo = secretCrushRequestRepo;
        this.postLikeRepo = postLikeRepo;
        this.authUtils = authUtils;
    }



    // ************** Fetch search profile securely ***************
    
    public SearchUserResponse userProfile(String username) {

        // 1️⃣ Get logged-in user
        Users loggedUser = authUtils.getLoggedUser();

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
                        boolean blockedbyme = blockRepo.existsByBlockerAndBlocked(loggedUser, timelineUser);
                        boolean blockedme = blockRepo.existsByBlockerAndBlocked(timelineUser, loggedUser);
                        if(blockedbyme || blockedme){
                            res.setSearchUserTimeline(null);
                        }else{
                            res.setSearchUserTimeline(timelineUser.getFullname());
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
        res.setFollowingCount(followRepo.countByFollower_UserId(user.getUserId()));
        res.setPostCount(postRepo.countByUserpost_UserId(user.getUserId()));

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

        Set<Long> likedPostIds = postIds.isEmpty() ? Collections.emptySet() :
                postLikeRepo.findLikedPostIdsByUserAndPostIds(loggedUser, postIds);

        Set<Long> savedPostIds = postIds.isEmpty() ? Collections.emptySet() :
                savedPostRepo.findSavedPostIdsByUserAndPostIds(loggedUser, postIds);

        // Batch Fetch Blocked Users
        List<Long> blockedByMe = blockRepo.findBlockedUsers(loggedUser)
            .stream()
            .map(block -> block.getUserId())
            .collect(Collectors.toList());

        List<Long> blockedMe = blockRepo.findBlockedByUsers(loggedUser)
            .stream()
            .map(block -> block.getUserId())
            .collect(Collectors.toList());

        for(PostsEntity post : posts){

            PostFetchDTO dto = new PostFetchDTO();

            dto.setFetchPostId(String.valueOf(post.getPostId()));
            dto.setFetchFileName(post.getFileName());
            dto.setFetchPostLocation(post.getPostLocation());
            dto.setFetchPostCaption(post.getPostCaption());
            dto.setFetchTimelineUser(String.valueOf(post.getTimelineUser()));
            dto.setFetchUploadAt(post.getUploadAt());
            dto.setFetchVerified(userRes.isVerifyTag());

            // Set Tagged Users
            // 1. Find all tagged user for the post 
            List<String> taggedUsersId = new ArrayList<>();
            if(post.getTaggedUsers() != null && !post.getTaggedUsers().isEmpty()){
                for(String taggedUser : post.getTaggedUsers()){
                    Long taggedUserId;
                    try {
                        taggedUserId = Long.valueOf(taggedUser);
                    } catch (NumberFormatException e) {
                        continue; // Ignore invalid tagged user IDs
                    }

                    // Check if tagged user is blocked by logged-in user or blocked logged-in user
                    if(blockedByMe.contains(taggedUserId) || blockedMe.contains(taggedUserId)){
                        continue; // Skip this tagged user
                    }
                    // Add to the list of tagged users
                    taggedUsersId.add(taggedUser);
                }
            }

            // 2. Get the list of tagged users' details from the database
            List<Users> taggedUsers = usersRepo.findTaggedUsersByIds(taggedUsersId);

            // 3. Convert to DTOs
            List<TaggingResult> taggingResults = new ArrayList<>();
            for(Users user : taggedUsers){
                TaggingResult dtoTag = new TaggingResult();
                dtoTag.setUserId(user.getUserId().toString());
                dtoTag.setUsername(user.getUsername());
                dtoTag.setVerify(user.isVerifyTag());
                if (user.getUserData() != null) {
                    dtoTag.setProfileImage(user.getUserData().getProfilePhoto());
                }
                taggingResults.add(dtoTag);
            }
            dto.setFetchTaggedUsers(taggingResults);

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
            dto.setLikeVisible(post.getLikeVisible());
            dto.setShareEnable(post.getShareEnabled());
            dto.setOwnPost(post.getUserpost().getUserId().equals(loggedUser.getUserId()));

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

        Set<Long> likedPostIds = postIds.isEmpty() ? Collections.emptySet() :
                postLikeRepo.findLikedPostIdsByUserAndPostIds(loggedUser, postIds);

        Set<Long> savedPostIds = postIds.isEmpty() ? Collections.emptySet() :
                savedPostRepo.findSavedPostIdsByUserAndPostIds(loggedUser, postIds);

        // Batch Fetch Blocked Users
        List<Long> blockedByMe = blockRepo.findBlockedUsers(loggedUser)
            .stream()
            .map(block -> block.getUserId())
            .collect(Collectors.toList());

        List<Long> blockedMe = blockRepo.findBlockedByUsers(loggedUser)
            .stream()
            .map(block -> block.getUserId())
            .collect(Collectors.toList());
        
        for(PostsEntity post : posts){

            PostFetchDTO dto = new PostFetchDTO();

            dto.setFetchPostId(String.valueOf(post.getPostId()));
            dto.setFetchFileName(post.getFileName());
            dto.setFetchPostLocation(post.getPostLocation());
            dto.setFetchPostCaption(post.getPostCaption());
            dto.setFetchTimelineUser(String.valueOf(post.getTimelineUser()));
            dto.setFetchUploadAt(post.getUploadAt());

            // Set Tagged Users
            // 1. Find all tagged user for the post 
            List<String> taggedUsersId = new ArrayList<>();
            if(post.getTaggedUsers() != null && !post.getTaggedUsers().isEmpty()){
                for(String taggedUser : post.getTaggedUsers()){
                    Long taggedUserId;
                    try {
                        taggedUserId = Long.valueOf(taggedUser);
                    } catch (NumberFormatException e) {
                        continue; // Ignore invalid tagged user IDs
                    }

                    // Check if tagged user is blocked by logged-in user or blocked logged-in user
                    if(blockedByMe.contains(taggedUserId) || blockedMe.contains(taggedUserId)){
                        continue; // Skip this tagged user
                    }
                    // Add to the list of tagged users
                    taggedUsersId.add(taggedUser);
                }
            }

            // 2. Get the list of tagged users' details from the database
            List<Users> taggedUsers = usersRepo.findTaggedUsersByIds(taggedUsersId);

            // 3. Convert to DTOs
            List<TaggingResult> taggingResults = new ArrayList<>();
            for(Users user : taggedUsers){
                TaggingResult dtoTag = new TaggingResult();
                dtoTag.setUserId(user.getUserId().toString());
                dtoTag.setUsername(user.getUsername());
                dtoTag.setVerify(user.isVerifyTag());
                if (user.getUserData() != null) {
                    dtoTag.setProfileImage(user.getUserData().getProfilePhoto());
                }
                taggingResults.add(dtoTag);
            }
            dto.setFetchTaggedUsers(taggingResults);
            

            // Set Post User Details
            dto.setFullname(post.getUserpost().getFullname());
            dto.setUserId(String.valueOf(post.getUserpost().getUserId()));
            dto.setUsername(post.getUserpost().getUsername());
            if(post.getUserpost().getUserData() != null){
                dto.setProfileImage(post.getUserpost().getUserData().getProfilePhoto());
            }
            dto.setFetchVerified(userRes.isVerifyTag());

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
            dto.setLikeVisible(post.getLikeVisible());
            dto.setShareEnable(post.getShareEnabled());
            dto.setOwnPost(post.getUserpost().getUserId().equals(loggedUser.getUserId()));

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

        // check if user is private and not following or if logged user blocked the search user then return nothing
        boolean isBlockedByLoggedUser = blockRepo.existsByBlockerAndBlocked(loggedUser, userRes);
        if(isBlockedByLoggedUser || (userRes.isStatusPrivate() && !isFollowingPvt)){
            return Collections.emptyList();
        }

        //  self check 
        if(userRes.getUserId().equals(loggedUser.getUserId())){
            isFollowingPvt = true;
        }


        // check if postowner blocked me or blocked by me them return nothing
        List<Long> blockedByMe = blockRepo.findBlockedUsers(loggedUser)
            .stream()
            .map(block -> block.getUserId())
            .collect(Collectors.toList());

        List<Long> blockedMe = blockRepo.findBlockedByUsers(loggedUser)
            .stream()
            .map(block -> block.getUserId())
            .collect(Collectors.toList());


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

            // Hide private users' posts unless the logged-in user follows them
            if (post.getUserpost().isStatusPrivate() && !followedUserIds.contains(postOwnerUserId)) {
                continue;
            }

            PostFetchDTO dto = new PostFetchDTO();

            dto.setFetchPostId(String.valueOf(post.getPostId()));
            dto.setFetchFileName(post.getFileName());
            dto.setFetchPostLocation(post.getPostLocation());
            dto.setFetchPostCaption(post.getPostCaption());
            dto.setFetchTimelineUser(String.valueOf(post.getTimelineUser()));
            dto.setFetchUploadAt(post.getUploadAt());
            dto.setFetchVerified(userRes.isVerifyTag());

            // Set Tagged Users
            // 1. Find all tagged user for the post 
            List<String> taggedUsersId = new ArrayList<>();
            if(post.getTaggedUsers() != null && !post.getTaggedUsers().isEmpty()){
                for(String taggedUser : post.getTaggedUsers()){
                    Long taggedUserId;
                    try {
                        taggedUserId = Long.valueOf(taggedUser);
                    } catch (NumberFormatException e) {
                        continue; // Ignore invalid tagged user IDs
                    }

                    // Check if tagged user is blocked by logged-in user or blocked logged-in user
                    if(blockedByMe.contains(taggedUserId) || blockedMe.contains(taggedUserId)){
                        continue; // Skip this tagged user
                    }
                    // Add to the list of tagged users
                    taggedUsersId.add(taggedUser);
                }
            }

            // 2. Get the list of tagged users' details from the database
            List<Users> taggedUsers = usersRepo.findTaggedUsersByIds(taggedUsersId);

            // 3. Convert to DTOs
            List<TaggingResult> taggingResults = new ArrayList<>();
            for(Users user : taggedUsers){
                TaggingResult dtoTag = new TaggingResult();
                dtoTag.setUserId(user.getUserId().toString());
                dtoTag.setUsername(user.getUsername());
                dtoTag.setVerify(user.isVerifyTag());
                if (user.getUserData() != null) {
                    dtoTag.setProfileImage(user.getUserData().getProfilePhoto());
                }
                taggingResults.add(dtoTag);
            }
            dto.setFetchTaggedUsers(taggingResults);

            // Set Post User Details
            dto.setFullname(post.getUserpost().getFullname());
            dto.setUserId(String.valueOf(post.getUserpost().getUserId()));
            dto.setUsername(post.getUserpost().getUsername());
            dto.setFetchVerified(post.getUserpost().isVerifyTag());
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
            dto.setLikeVisible(post.getLikeVisible());
            dto.setShareEnable(post.getShareEnabled());
            dto.setOwnPost(post.getUserpost().getUserId().equals(loggedUser.getUserId()));

            // Set Like Flag
            dto.setLikedByCurrentUser(likedPostIds.contains(post.getPostId()));
            dto.setSavedByCurrentUser(savedPostIds.contains(post.getPostId()));

            postsList.add(dto);
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
        Set<Long> iBlocked = blockRepo.findBlockedUserIds(userOne);
        Set<Long> blockedMe = blockRepo.findBlockedByUserIds(userOne);

        // Filter blocked users
        List<Users> filteredFollowers = followers.stream()
                .filter(f -> !iBlocked.contains(f.getUserId()) 
                        && !blockedMe.contains(f.getUserId()))
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
                resData.setuTimeline(true);
            }else{
                resData.setuTimeline(false);
            }
            
        }

        return resData;
    }
    
}