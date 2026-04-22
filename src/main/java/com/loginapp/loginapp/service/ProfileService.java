package com.loginapp.loginapp.service;

import org.springframework.data.domain.Pageable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.loginapp.loginapp.DTO.LoggedUserResponse;
import com.loginapp.loginapp.DTO.PostFetchDTO;
import com.loginapp.loginapp.DTO.SearchUserResponse;
import com.loginapp.loginapp.config.CorsConfig;
import com.loginapp.loginapp.entity.PostMedia;
import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.UserData;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.FollowRepo;
import com.loginapp.loginapp.repository.FollowRequestRepo;
import com.loginapp.loginapp.repository.PostMediaRepo;
import com.loginapp.loginapp.repository.PostRepo;
import com.loginapp.loginapp.repository.UsersRepo;

import java.util.*;

@Service
public class ProfileService {

    private final CorsConfig corsConfig;

    @Autowired 
    private UsersRepo usersRepo;

    @Autowired
    private FollowRepo followRepo;

    @Autowired
    private PostRepo postRepo;

    @Autowired
    private FollowRequestRepo followRequestRepo;

    @Autowired
    private PostMediaRepo postMediaRepo;

    ProfileService(CorsConfig corsConfig) {
        this.corsConfig = corsConfig;
    }

    // Fetch search profile securely using projection
    public SearchUserResponse userProfile(String username) {

        // 1️⃣ Get logged-in user
        Long userUid = Long.parseLong(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        Users loggedUser = usersRepo.findByUserId(userUid)
                .orElseThrow(() -> new IllegalArgumentException("Something went wrong!"));

        // 2️⃣ Get searched user
        Users user = usersRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 3️⃣ Check deleted user
        if (user.isStatusDeleted()) {
            throw new IllegalArgumentException("User not found");
        }

        // 4️⃣ Prepare response object
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
                usersRepo.findByUserId(data.getTimeUser())
                        .ifPresent(timelineUser ->
                                res.setSearchUserTimeline(timelineUser.getFullname())
                        );
            }
        }

        // ================= PRIVATE LOGIC =================
        boolean isPrivate = user.isStatusPrivate();
        boolean isFollowing = followRepo.existsByFollower_UserIdAndFollowing_UserId(userUid, user.getUserId());

        res.setSearchPrivate(isPrivate);
        res.setSearchPrivateShow(!isPrivate || isFollowing);

        // ================= SELF PROFILE =================
        if (user.getUserId().equals(userUid)) {

            res.setSearchLoggedUser(true);
            res.setFollowingStatus(false);
            res.setFollowerStatus(false);
            res.setFollowReqStatus(false);
            res.setFollowReqOptStatus(false);

            // self always visible
            res.setSearchPrivate(false);
            res.setSearchPrivateShow(true);

        } else {

            res.setSearchLoggedUser(false);

            boolean isFollower = followRepo.existsByFollower_UserIdAndFollowing_UserId(user.getUserId(), userUid);
            boolean isFollowReqSent = followRequestRepo.existsBySenderIdAndReceiverId(loggedUser, user);
            boolean isFollowReqReceived = followRequestRepo.existsBySenderIdAndReceiverId(user, loggedUser);

            res.setFollowingStatus(isFollowing);
            res.setFollowerStatus(isFollower);
            res.setFollowReqStatus(isFollowReqSent);
            res.setFollowReqOptStatus(isFollowReqReceived);
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
        Long userUid = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        Users userRes = usersRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userRes.isStatusDeleted()) {
            throw new IllegalArgumentException("User not found");
        }

        boolean isFollowingPvt = followRepo.existsByFollower_UserIdAndFollowing_UserId(userUid, userRes.getUserId());

        if(userRes.getUserId().equals(userUid)){
            isFollowingPvt = true; // Allow access to own posts 
        }
        if(userRes.isStatusPrivate() && !isFollowingPvt){
            return Collections.emptyList();
        }

        Pageable pageable = PageRequest.of(page, 10);

        List<PostsEntity> posts = postRepo.findUserPosts(userRes, pageable);

        List<PostFetchDTO> postsList = new ArrayList<>();

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

            PostMedia media = postMediaRepo.findByPost(post).orElse(null);
            if (media != null) {
                dto.setWidth(media.getWidth());
                dto.setHeight(media.getHeight());
                dto.setDuration(media.getDuration());

                if (media.getPostType() != null) {
                    dto.setPostType(media.getPostType().name());
                }
            }

            dto.setCommentCount(post.getCommentCount()+"");
            dto.setLikeCount(post.getLikeCount()+"");
            dto.setSaveCount(post.getSaveCount()+"");
            dto.setViewCount(post.getViewCount()+"");

            dto.setCommentEnable(post.getCommentEnabled());
            dto.setLikeHide(post.getLikeVisible());
            dto.setShareEnable(post.getShareEnabled());

            postsList.add(dto);
        }

        return postsList;
    }

    // Search User TimeLine Post 
    public List<PostFetchDTO> getSearchUserTimelinePosts(String username, int page){

        // Current User
        Long userUid = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        // Search User Found
        Users userRes = usersRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userRes.isStatusDeleted()) {
            throw new IllegalArgumentException("User not found");
        }

        boolean isFollowingPvt = followRepo.existsByFollower_UserIdAndFollowing_UserId(userUid, userRes.getUserId());

        if(userRes.isStatusPrivate() && !isFollowingPvt){
            return Collections.emptyList();
        }

        Pageable pageable = PageRequest.of(page, 10);

        // Get details of Timeline User 
        Users timelineUser = usersRepo.findByUserId(userRes.getUserData().getTimeUser())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<PostsEntity> posts = postRepo.findUserPosts(timelineUser, pageable);

        List<PostFetchDTO> postsList = new ArrayList<>();

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

            PostMedia media = postMediaRepo.findByPost(post).orElse(null);
            if (media != null) {
                dto.setWidth(media.getWidth());
                dto.setHeight(media.getHeight());
                dto.setDuration(media.getDuration());
                dto.setPostType(media.getPostType().name());
            }

            dto.setCommentCount(post.getCommentCount()+"");
            dto.setLikeCount(post.getLikeCount()+"");
            dto.setSaveCount(post.getSaveCount()+"");
            dto.setViewCount(post.getViewCount()+"");

            dto.setCommentEnable(post.getCommentEnabled());
            dto.setLikeHide(post.getLikeVisible());
            dto.setShareEnable(post.getShareEnabled());

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
        // 🔹 Null-safe userData fetch
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
