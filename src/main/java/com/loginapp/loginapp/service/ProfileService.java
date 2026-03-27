package com.loginapp.loginapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.loginapp.loginapp.DTO.LoggedUserResponse;
import com.loginapp.loginapp.DTO.PostFetchDTO;
import com.loginapp.loginapp.DTO.SearchUserResponse;
import com.loginapp.loginapp.config.CorsConfig;
import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.UserData;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.FollowRepo;
import com.loginapp.loginapp.repository.FollowRequestRepo;
import com.loginapp.loginapp.repository.PostRepo;
import com.loginapp.loginapp.repository.UsersRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    ProfileService(CorsConfig corsConfig) {
        this.corsConfig = corsConfig;
    }

    // Fetch search profile securely using projection
    public SearchUserResponse userProfile(String username) {

        // 1️⃣ Get logged-in username from JWT
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userUid = Long.parseLong(userIdStr);
        Users userUidRes = usersRepo.findByUserId(userUid)
                .orElseThrow(() -> new IllegalArgumentException("Something went wrong!"));

        // Check user is found or not ...
        Users userRes = usersRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check User is deleted / deactivate or not ...
        if (userRes.isStatusDeleted() == true) {
            throw new IllegalArgumentException("User not found");
        }

        // Now return user data after found
        SearchUserResponse response = new SearchUserResponse();

        // check verify 
        boolean verifiedTemp = false;
        if(userRes.isVerifyTag() == true){
            verifiedTemp = true;
        }else{
            verifiedTemp = false;
        }

        // check private 
        boolean privateAccTemp = false;
        if(userRes.isStatusPrivate() == true){
            privateAccTemp = true;
        }else{
            privateAccTemp = false;
        }

        // User Details..
        response.setSearchUserId(String.valueOf(userRes.getUserId()));
        response.setSearchUsername(userRes.getUsername());
        response.setSearchFullname(userRes.getFullname());
        response.setSearchVerified(verifiedTemp);
        response.setSearchCreatedAt(userRes.getCreatedAt());

        // User Data..
        if(userRes.getUserData() != null){
            UserData userDataRes = userRes.getUserData();
            response.setSearchProfilePhoto(userDataRes.getProfilePhoto());
            response.setSearchUserBio(userDataRes.getUserBio());
            response.setSearchUserLocation(userDataRes.getUserLocation());
            response.setSearchUserLink(userDataRes.getUserlink());
            response.setSearchBadge(userDataRes.getBadge());
            response.setSearchUserGender(userDataRes.getUserGender());

            Optional<Users> timelineUserData = usersRepo.findByUserId(userDataRes.getTimeUser());
            if (!timelineUserData.isEmpty()) {
                response.setSearchUserTimeline(timelineUserData.get().getFullname()); 
            }
        }

        

        // Public or Private Account Flags 
        response.setSearchPrivate(privateAccTemp);
        boolean isFollowingPvt = followRepo.existsByFollower_UserIdAndFollowing_UserId(userUid, userRes.getUserId());
        if(privateAccTemp == true && isFollowingPvt == true){
            response.setSearchPrivateShow(true);
        }else if(privateAccTemp == true && isFollowingPvt == false){
            response.setSearchPrivateShow(false);
        }else{
            response.setSearchPrivateShow(true);
        }




        // User Flags ...
        if(userRes.getUserId().equals(userUid)){
            response.setSearchLoggedUser(true);
            response.setFollowReqStatus(false);
            response.setFollowReqOptStatus(false);
            response.setFollowingStatus(false); // self profile
            response.setFollowerStatus(false); // self profile
            response.setSearchPrivate(false); // self
            response.setSearchPrivateShow(true); // self
        }else{
            response.setSearchLoggedUser(false);

            // Precompute all 4 checks in advance (avoids nested ifs)
            boolean isFollowing = followRepo.existsByFollower_UserIdAndFollowing_UserId(userUid, userRes.getUserId());
            boolean isFollower = followRepo.existsByFollower_UserIdAndFollowing_UserId(userRes.getUserId(), userUid);
            boolean isFollowReqSent = followRequestRepo.existsBySenderIdAndReceiverId(userUidRes, userRes);
            boolean isFollowReqReceived = followRequestRepo.existsBySenderIdAndReceiverId(userRes, userUidRes);

            // Set flags directly
            response.setFollowingStatus(isFollowing);
            response.setFollowerStatus(isFollower);
            response.setFollowReqStatus(isFollowReqSent);
            response.setFollowReqOptStatus(isFollowReqReceived);
        }


        // Count Follower and Following 
        response.setFollowersCount(followRepo.countByFollowing_UserId(userRes.getUserId()));
        response.setFollowingCount(followRepo.countByFollower_UserId(userRes.getUserId()));
        long postCount = postRepo.countByUserpost_UserId(userRes.getUserId());
        response.setPostCount(postCount);

        // Set Posts 
        List<PostFetchDTO> postsList = new ArrayList<>();
        List<PostFetchDTO> postsTimeline = new ArrayList<>();
        if(userRes.getPostsEntity() != null){
            for(PostsEntity post : userRes.getPostsEntity()){
                PostFetchDTO dto = new PostFetchDTO();

                // User Main Posts 
                dto.setFetchPostId(String.valueOf(post.getPostId()));
                dto.setFetchFileName(post.getFileName());
                dto.setFetchPostLocation(post.getPostLocation());
                dto.setFetchPostCaption(post.getPostCaption());
                dto.setFetchTaggedUsers(post.getTaggedUsers()); // assuming it's List<String>
                dto.setFetchTimelineUser(String.valueOf(post.getTimelineUser()));
                dto.setFetchUploadAt(post.getUploadAt());
                dto.setFetchVerified(verifiedTemp);
                postsList.add(dto);

                // Time Post Adding 
                if(post.getTimelineUser() != null && userRes.getUserData() != null && userRes.getUserData().getTimeUser() != null && userRes.getUserData().getTimeUser().equals(post.getTimelineUser())){
                    postsTimeline.add(dto);
                }
            }
        }

        response.setUserPosts(postsList);
        response.setTimelinePosts(postsTimeline);

        // Return response 
        return response;

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
