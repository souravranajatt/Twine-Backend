package com.loginapp.loginapp.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.loginapp.loginapp.DTO.FollowRequest;
import com.loginapp.loginapp.entity.FollowRequestTable;
import com.loginapp.loginapp.entity.FollowUser;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.FollowRepo;
import com.loginapp.loginapp.repository.FollowRequestRepo;
import com.loginapp.loginapp.repository.UsersRepo;

@Service
public class ProfileActionService {
     
    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private FollowRepo followRepo;

    @Autowired
    private FollowRequestRepo followRequestRepo;

    // Follow User Logic..
    public void followUserAction(FollowRequest followRequest){

        // 1️⃣ Get logged-in username from JWT
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userUid = Long.parseLong(userIdStr);
        Users userOne = usersRepo.findByUserId(userUid)
                              .orElseThrow(() -> new IllegalArgumentException("Something went wrong!"));

        // 2. Target user
        String searchUidStr = followRequest.getUserUid();
        if(searchUidStr == null || searchUidStr.isEmpty()){
            throw new IllegalArgumentException("Target ID is missing!");
        }
        Long searchUid = Long.parseLong(searchUidStr);
        Users userTwo = usersRepo.findByUserId(searchUid)
                            .orElseThrow(() -> new IllegalArgumentException("User not found!"));

        // Check search user id is soft deactivate or not 
        if(userTwo.isStatusDeleted() == true){
            throw new IllegalArgumentException("User is not available!");
        }
        
        // Check both user same or not 
        if(userOne.getUserId().equals(userTwo.getUserId())){
            throw new IllegalArgumentException("You can't follow yourself!");
        }

        // Check that user follow this account or not
        Optional<FollowUser> followOpt = followRepo.findByFollowerAndFollowing(userOne, userTwo);
        if(followOpt.isPresent()){
            // Unfollow the account
            followRepo.delete(followOpt.get());
            return;
        }

        // check if account private or not 
        if(userTwo.isStatusPrivate() == true){
          // Send a request 
          Optional<FollowRequestTable> reqOpt = followRequestRepo.findBySenderIdAndReceiverId(userOne, userTwo);
            if (reqOpt.isPresent()) {
                followRequestRepo.delete(reqOpt.get());  // Cancel request
            } else {
                FollowRequestTable req = new FollowRequestTable();
                req.setSenderId(userOne);
                req.setReceiverId(userTwo);
                followRequestRepo.save(req);  // Send request
            }
            return;
        }

        FollowUser respFollowUser = new FollowUser();
        respFollowUser.setFollower(userOne);
        respFollowUser.setFollowing(userTwo);
        followRepo.save(respFollowUser);
    }


    // Follow Request Handle Logic ...
    public void followRequestHanle(FollowRequest followRequest){

        // 1️⃣ Get logged-in username from JWT
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userUid = Long.parseLong(userIdStr);
        Users userOne = usersRepo.findByUserId(userUid)
                              .orElseThrow(() -> new IllegalArgumentException("Something went wrong!"));

        // 2. Target user
        String searchUidStr = followRequest.getUserUid();
        if(searchUidStr == null || searchUidStr.isEmpty()){
            throw new IllegalArgumentException("Target ID is missing!");
        }
        Long searchUid = Long.parseLong(searchUidStr);
        Users userTwo = usersRepo.findByUserId(searchUid)
                            .orElseThrow(() -> new IllegalArgumentException("User not found!"));

        // Check search user id is soft deactivate or not 
        if(userTwo.isStatusDeleted() == true){
            throw new IllegalArgumentException("User is not available!");
        }
        
        // Check both user same or not 
        if(userOne.getUserId().equals(userTwo.getUserId())){
            throw new IllegalArgumentException("You can't follow yourself!");
        }

        // Logic for Accepting follow request 
        Optional<FollowRequestTable> reqOpt =
        followRequestRepo.findBySenderIdAndReceiverId(userTwo, userOne);

        if(reqOpt.isEmpty()){
            return;
        }

        String action = followRequest.getActionType();

        if(action == null){
            throw new IllegalArgumentException("Action type required!");
        }

        if("ACCEPT".equalsIgnoreCase(action)){
            FollowUser acceptRequest = new FollowUser();
            acceptRequest.setFollower(userTwo);
            acceptRequest.setFollowing(userOne);
            followRepo.save(acceptRequest);

            followRequestRepo.delete(reqOpt.get());
            return;
        }

        if("REJECT".equalsIgnoreCase(action)){
            followRequestRepo.delete(reqOpt.get());
            return;
        }
    }

}