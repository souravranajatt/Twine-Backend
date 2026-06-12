package com.loginapp.loginapp.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loginapp.loginapp.Utils.AuthUtils;
import com.loginapp.loginapp.entity.BlockUser;
import com.loginapp.loginapp.entity.FollowRequestTable;
import com.loginapp.loginapp.entity.FollowUser;
import com.loginapp.loginapp.entity.SecretCrushRelation;
import com.loginapp.loginapp.entity.SecretCrushRequest;
import com.loginapp.loginapp.entity.UserData;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.BlockRepo;
import com.loginapp.loginapp.repository.FollowRepo;
import com.loginapp.loginapp.repository.FollowRequestRepo;
import com.loginapp.loginapp.repository.SecretCrushRepo;
import com.loginapp.loginapp.repository.SecretCrushRequestRepo;
import com.loginapp.loginapp.repository.UsersRepo;

@Service
@Transactional
public class ProfileActionService {

    // Inject Other Files thorugh constructor
     
    private final AuthUtils authUtils;

    private final UsersRepo usersRepo;

    private final FollowRepo followRepo;

    private final FollowRequestRepo followRequestRepo;

    private final BlockRepo blockRepo;

    private final SecretCrushRepo secretCrushRepo;

    private final SecretCrushRequestRepo secretCrushRequestRepo;

    ProfileActionService(SecretCrushRequestRepo secretCrushRequestRepo, AuthUtils authUtils, UsersRepo usersRepo, FollowRepo followRepo, FollowRequestRepo followRequestRepo, BlockRepo blockRepo, SecretCrushRepo secretCrushRepo) {
        this.secretCrushRequestRepo = secretCrushRequestRepo;
        this.authUtils = authUtils;
        this.usersRepo = usersRepo;
        this.followRepo = followRepo;
        this.followRequestRepo = followRequestRepo;
        this.blockRepo = blockRepo;
        this.secretCrushRepo = secretCrushRepo;
    }

    // 1. Follow User Logic..
    public void followUser(Long targetUserId) {

        // Get logged user details from security context
        Users userOne = authUtils.getLoggedUser();

        Users userTwo = usersRepo.findByUserId(targetUserId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found!"));

        if (userTwo.isStatusDeleted())
            throw new IllegalArgumentException("User is not available!");

        if (userOne.getUserId().equals(userTwo.getUserId()))
            throw new IllegalArgumentException("You can't follow yourself!");

        boolean isBlocked = blockRepo.existsByBlockerAndBlocked(userOne, userTwo)
                        || blockRepo.existsByBlockerAndBlocked(userTwo, userOne);
        if (isBlocked)
            throw new IllegalArgumentException("Action not allowed! User is blocked.");

        // Already following check
        boolean alreadyFollowing = followRepo.existsByFollowerAndFollowing(userOne, userTwo);
        if (alreadyFollowing)
            throw new IllegalArgumentException("Already following!");

        // Private account — send request
        if (userTwo.isStatusPrivate()) {
            boolean alreadyRequested = followRequestRepo.existsBySenderIdAndReceiverId(userOne, userTwo);
            if (alreadyRequested)
                throw new IllegalArgumentException("Request already sent!");

            FollowRequestTable req = new FollowRequestTable();
            req.setSenderId(userOne);
            req.setReceiverId(userTwo);
            followRequestRepo.save(req);
            return;
        }

        // Public account — direct follow
        FollowUser follow = new FollowUser();
        follow.setFollower(userOne);
        follow.setFollowing(userTwo);
        followRepo.save(follow);
    }


    // 2. Unfollow User Logic..
    public void unfollowUser(Long targetUserId) {

        // Get logged user details from security context
        Users userOne = authUtils.getLoggedUser();

        Users userTwo = usersRepo.findByUserId(targetUserId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found!"));

        if (userTwo.isStatusDeleted())
            throw new IllegalArgumentException("User is not available!");

        if (userOne.getUserId().equals(userTwo.getUserId()))
            throw new IllegalArgumentException("You can't unfollow yourself!");

        // Check follow exists
        FollowUser follow = followRepo.findByFollowerAndFollowing(userOne, userTwo)
                            .orElseThrow(() -> new IllegalArgumentException("Not following!"));

        followRepo.delete(follow);
    }


    // 3. Cancel Follow Request ..
    public void cancelFollowRequest(Long targetUserId) {

        // Get logged user details from security context
        Users userOne = authUtils.getLoggedUser();

        Users userTwo = usersRepo.findByUserId(targetUserId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found!"));

        if (userTwo.isStatusDeleted())
            throw new IllegalArgumentException("User is not available!");

        if (userOne.getUserId().equals(userTwo.getUserId()))
            throw new IllegalArgumentException("Invalid action!");

        // Check request exists
        FollowRequestTable req = followRequestRepo.findBySenderIdAndReceiverId(userOne, userTwo)
                                .orElseThrow(() -> new IllegalArgumentException("No request found!"));

        followRequestRepo.delete(req);
    }



    // 4. Block User Logic ...
    public String blockUserAction(Long targetUserId){
        
        // 1️⃣ Get logged-in username from JWT
        Users userOne = authUtils.getLoggedUser();
        
        // 2. Target user
        Users userTwo = usersRepo.findByUserId(targetUserId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found!"));

        // Check search user id is soft deactivate or not 
        if(userTwo.isStatusDeleted()){
            throw new IllegalArgumentException("User is not available!");   
        }
        // Check both user are same or not
        if(userOne.getUserId().equals(userTwo.getUserId())){
            throw new IllegalArgumentException("You can't block yourself!");
        }

        // Check that user block this account or not
        boolean isBlocked = blockRepo.existsByBlockerAndBlocked(userOne, userTwo);
        if(isBlocked){
            throw new IllegalArgumentException("User is already blocked!");
        }   
        // Block the account
        BlockUser blockUser = new BlockUser();
        blockUser.setBlocker(userOne);
        blockUser.setBlocked(userTwo);
        blockRepo.save(blockUser);

        // Delete Follow relationship if exists

        followRepo.deleteByFollower_UserIdAndFollowing_UserId(userOne.getUserId(), userTwo.getUserId());
        followRepo.deleteByFollower_UserIdAndFollowing_UserId(userTwo.getUserId(), userOne.getUserId());
        followRequestRepo.deleteBySenderIdAndReceiverId(userOne, userTwo);
        followRequestRepo.deleteBySenderIdAndReceiverId(userTwo, userOne);

        // Relation CleanUp if exists
        secretCrushRepo.deleteByUserOneAndUserTwo(userOne, userTwo);
        secretCrushRepo.deleteByUserOneAndUserTwo(userTwo, userOne);
        secretCrushRequestRepo.deleteBySenderIdAndAnonymousId(userOne, userTwo);
        secretCrushRequestRepo.deleteBySenderIdAndAnonymousId(userTwo, userOne);

        // User Data Cleanup if exists
        if(userOne.getUserData() != null && 
        userTwo.getUserId().equals(userOne.getUserData().getTimeUser())){
            userOne.getUserData().setTimeUser(null);
            usersRepo.save(userOne);
        }

        // userTwo ka timeUser sirf tab null karo jab userOne se match ho
        if(userTwo.getUserData() != null && 
        userOne.getUserId().equals(userTwo.getUserData().getTimeUser())){
            userTwo.getUserData().setTimeUser(null);
            usersRepo.save(userTwo);
        }

        return "Blocked successfully!";
    }



    // Unblock User Logic ...
    public String unblockUserAction(Long targetUserId){
        // 1️⃣ Get logged-in username from JWT
        Users userOne = authUtils.getLoggedUser();
        
        // 2. Target user
        Users userTwo = usersRepo.findByUserId(targetUserId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found!"));

        // Check search user id is soft deactivate or not 
        if(userTwo.isStatusDeleted()){
            throw new IllegalArgumentException("User is not available!");   
        }
        // Check both user same or not
        if(userOne.getUserId().equals(userTwo.getUserId())){
            throw new IllegalArgumentException("You can't unblock yourself!");
        }

        // Check that user block this account or not
        boolean isBlocked = blockRepo.existsByBlockerAndBlocked(userOne, userTwo);
        if(!isBlocked){
            throw new IllegalArgumentException("User is not blocked!");
        }
        
        // 4. Unblock
        blockRepo.deleteByBlockerAndBlocked(userOne, userTwo);
        
        return "Unblocked successfully!";
    }



    // Send Anonymous Like Logic ...
    public void sendAnonymousLike(Long targetUserId){
        //  Get logged-in username from JWT
        Users userOne = authUtils.getLoggedUser();
        
        // Target user
        Users userTwo = usersRepo.findByUserId(targetUserId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found!"));


        // Check search user id is soft deactivate or not 
        if(userTwo.isStatusDeleted()){
            throw new IllegalArgumentException("User is not available!");   
        }
        // Check both user same or not
        if(userOne.getUserId().equals(userTwo.getUserId())){
            throw new IllegalArgumentException("You can't send to yourself!");
        }

        // Check if user has blocked the other user
        boolean isBlocked = blockRepo.existsByBlockerAndBlocked(userOne, userTwo)
                        || blockRepo.existsByBlockerAndBlocked(userTwo, userOne);
        if(isBlocked){
            throw new IllegalArgumentException("Action not allowed! User is blocked.");
        }

        boolean alreadyMatched = secretCrushRepo.existsByUserOneAndUserTwo(userOne, userTwo)
                      || secretCrushRepo.existsByUserOneAndUserTwo(userTwo, userOne);
        if(alreadyMatched){
            throw new IllegalArgumentException("Already matched!");
        }

        //  Already sent request check
        boolean alreadySent = secretCrushRequestRepo.existsBySenderIdAndAnonymousId(userOne, userTwo);
        if (alreadySent) {
            throw new IllegalArgumentException("Already sent!");
        }

        //  Check reciprocal request
        boolean reciprocalRequest = secretCrushRequestRepo.existsBySenderIdAndAnonymousId(userTwo, userOne);
        if (reciprocalRequest) {

            // Delete old relation of both users
            secretCrushRepo.deleteByUser(userTwo);
            secretCrushRepo.deleteByUser(userOne);

            // UserOne ka purana match null karo
            if (userOne.getUserData() != null && userOne.getUserData().getTimeUser() != null) {
                Long oldMatchOfUserOne = userOne.getUserData().getTimeUser();
                usersRepo.findByUserId(oldMatchOfUserOne).ifPresent(oldMatch -> {
                    if (oldMatch.getUserData() != null) {
                        oldMatch.getUserData().setTimeUser(null);
                        usersRepo.save(oldMatch);
                    }
                });
            }

            // UserTwo ka purana match null karo
            if (userTwo.getUserData() != null && userTwo.getUserData().getTimeUser() != null) {
                Long oldMatchOfUserTwo = userTwo.getUserData().getTimeUser();
                usersRepo.findByUserId(oldMatchOfUserTwo).ifPresent(oldMatch -> {
                    if (oldMatch.getUserData() != null) {
                        oldMatch.getUserData().setTimeUser(null);
                        usersRepo.save(oldMatch);
                    }
                });
            }

            // Create new relation
            SecretCrushRelation relation = new SecretCrushRelation();

            relation.setUserOne(userOne);
            relation.setUserTwo(userTwo);
            secretCrushRepo.save(relation);

            // Delete old pending request
            secretCrushRequestRepo.deleteBySenderIdAndAnonymousId(userOne, userTwo);
            secretCrushRequestRepo.deleteBySenderIdAndAnonymousId(userTwo, userOne);

            // Entry into User Data 
            if (userOne.getUserData() != null) {
                userOne.getUserData().setTimeUser(userTwo.getUserId());
                usersRepo.save(userOne);
            }else{
                UserData newUserData = new UserData();
                newUserData.setTimeUser(userTwo.getUserId());
                userOne.setUserData(newUserData);
                usersRepo.save(userOne);
            }

            if( userTwo.getUserData() != null) {
                userTwo.getUserData().setTimeUser(userOne.getUserId());
                usersRepo.save(userTwo);
            }else{
                UserData newUserData = new UserData();
                newUserData.setTimeUser(userOne.getUserId());
                userTwo.setUserData(newUserData);
                usersRepo.save(userTwo);
            }

            return;
        }

        // Send new request
        SecretCrushRequest request = new SecretCrushRequest();
        request.setSenderId(userOne);
        request.setAnonymousId(userTwo);
        secretCrushRequestRepo.save(request);

        }

}