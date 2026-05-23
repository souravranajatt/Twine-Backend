package com.loginapp.loginapp.service;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.loginapp.loginapp.DTO.BlockedUserFetchDTO;
import com.loginapp.loginapp.DTO.ChangePasswordRequestDTO;
import com.loginapp.loginapp.DTO.DeactivateRequestDTO;
import com.loginapp.loginapp.DTO.SettingDataDTO;
import com.loginapp.loginapp.Utils.PasswordHashing;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.entity.UserData;
import com.loginapp.loginapp.entity.BlockUser;
import com.loginapp.loginapp.entity.FollowRequestTable;
import com.loginapp.loginapp.entity.FollowUser;
import com.loginapp.loginapp.repository.UsersRepo;
import com.loginapp.loginapp.repository.FollowRequestRepo;
import com.loginapp.loginapp.repository.BlockRepo;
import com.loginapp.loginapp.repository.FollowRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingService {
    
    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private FollowRequestRepo followRequestRepo;

    @Autowired
    private FollowRepo followRepo;

    @Autowired
    private PasswordHashing passwordHashing;

    @Autowired
    private BlockRepo blockRepo;

    // Username regex (only lowercase letters, numbers, underscore)
    private static final String USERNAME_REGEX = "^[a-z0-9_.]+$";
    private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);

    // Email regex
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    // Profile Data Fetch Setting Service
    public SettingDataDTO settingProfileData(){

        // Get UserId from Security Context JWT Token
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userUid = Long.parseLong(userIdStr);
        Users user = usersRepo.findByUserId(userUid)
                              .orElseThrow(() -> new IllegalArgumentException("Something went wrong!"));

        // Create and return SettingDataDTO
        SettingDataDTO settingDataDTO = new SettingDataDTO();
        settingDataDTO.setFullname(user.getFullname());
        settingDataDTO.setUsername(user.getUsername());
        settingDataDTO.setEmail(user.getEmail());
        settingDataDTO.setPrivateAccount(user.isStatusPrivate());
        
        if(user.getUserData() != null){
            settingDataDTO.setBio(user.getUserData().getUserBio());
            settingDataDTO.setLocation(user.getUserData().getUserLocation());
            settingDataDTO.setWebsiteLink(user.getUserData().getUserlink());
            settingDataDTO.setGender(user.getUserData().getUserGender());
            settingDataDTO.setProfilePictureUrl(user.getUserData().getProfilePhoto());
            settingDataDTO.setProfileBadge(user.getUserData().getBadge());
        }
        return settingDataDTO;
    }

    // Profile Data Update Setting Service
    public String settingProfileDataUpdate(SettingDataDTO updateDataDTO){

        // Get UserId from Security Context JWT Token
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userUid = Long.parseLong(userIdStr);
        Users user = usersRepo.findByUserId(userUid)
                              .orElseThrow(() -> new IllegalArgumentException("Something went wrong!"));

        // Update User Data
        
        // ====== 1. Null and Empty Checks ======
        if (updateDataDTO.getUsername() == null || updateDataDTO.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required!");
        }
        if (updateDataDTO.getFullname() == null || updateDataDTO.getFullname().trim().isEmpty()) {
            throw new IllegalArgumentException("Fullname is required!");
        }

        // ====== 2. Trim and Normalize Data ======
        String fullnameFinal = updateDataDTO.getFullname().trim();
        String usernameFinal = updateDataDTO.getUsername().trim().toLowerCase();

        if (fullnameFinal.length() > 30) {
            throw new IllegalArgumentException("Fullname can't exceed 30 characters!");
        }

        // ====== 4. Username Validation ======
        if (usernameFinal.length() > 25) {
            throw new IllegalArgumentException("Username can't exceed 25 characters!");
        }
        if (!USERNAME_PATTERN.matcher(usernameFinal).matches()) {
            throw new IllegalArgumentException("Username can only contain lowercase letters, digits, '.', and '_' !");
        }
        if (!user.getUsername().equals(usernameFinal) && usersRepo.findByUsername(usernameFinal).isPresent()) {
         throw new IllegalArgumentException("Username already taken!");
        }

        // Bio Validation
        if (updateDataDTO.getBio() != null && updateDataDTO.getBio().length() > 101) {
            throw new IllegalArgumentException("Bio can't exceed 101 characters!");
        }



        // ====== Update Users entity ======
        user.setFullname(fullnameFinal);
        user.setUsername(usernameFinal);

        // ====== Handle Cloudinary Base64 Photo Upload ======
        String photoStr = updateDataDTO.getProfilePictureUrl();
        if (photoStr != null && photoStr.trim().isEmpty()) {
            updateDataDTO.setProfilePictureUrl(null); // Save as null if removed/empty
        } else if (photoStr != null && photoStr.startsWith("data:image")) {
            try {
                String[] parts = photoStr.split(",");
                String base64Data = parts.length > 1 ? parts[1] : parts[0];
                String contentType = parts[0].split(";")[0].split(":")[1];
                
                // Sanitize base64 and use MimeDecoder to handle any whitespaces or newlines robustly
                String cleanBase64 = base64Data.replaceAll("\\s", "");
                byte[] imageBytes = java.util.Base64.getMimeDecoder().decode(cleanBase64);
                
                String fileName = "TWINE_PID" + user.getUserId() + "_" + System.currentTimeMillis();
                
                String newPhotoUrl = cloudinaryService.uploadFile(imageBytes, fileName, contentType);
                updateDataDTO.setProfilePictureUrl(newPhotoUrl);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to upload profile photo!");
            }
        }

        // ====== Update or Create UserData entity ======
        if(user.getUserData() != null){
            user.getUserData().setUserBio(updateDataDTO.getBio());
            user.getUserData().setUserLocation(updateDataDTO.getLocation());
            user.getUserData().setUserlink(updateDataDTO.getWebsiteLink());
            user.getUserData().setUserGender(updateDataDTO.getGender());
            user.getUserData().setProfilePhoto(updateDataDTO.getProfilePictureUrl());
            user.getUserData().setBadge(updateDataDTO.getProfileBadge());
        } else {
            UserData newUserData = new UserData();
            newUserData.setUsers(user); // Important: Link the users to userdata
            newUserData.setUserBio(updateDataDTO.getBio());
            newUserData.setUserLocation(updateDataDTO.getLocation());
            newUserData.setUserlink(updateDataDTO.getWebsiteLink());
            newUserData.setUserGender(updateDataDTO.getGender());
            newUserData.setProfilePhoto(updateDataDTO.getProfilePictureUrl());
            newUserData.setBadge(updateDataDTO.getProfileBadge());
            user.setUserData(newUserData); // Important: Set the new UserData on the Users entity
        }

        // Save Updated User Data
        usersRepo.save(user);
        return "Profile updated successfully!";
    }

    // Update Privacy Status Service
    @Transactional
    public String updatePrivacyPrivateStatus(boolean isPrivate) {
        // Get UserId from Security Context JWT Token
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userUid = Long.parseLong(userIdStr);
        Users user = usersRepo.findByUserId(userUid)
                              .orElseThrow(() -> new IllegalArgumentException("User not found!"));

        user.setStatusPrivate(isPrivate);
        usersRepo.save(user);

        // If account becomes Public, approve all pending follow requests
        if (!isPrivate) {
            List<FollowRequestTable> pendingRequests = followRequestRepo.findByReceiverId(user);
            
            if (!pendingRequests.isEmpty()) {
                List<FollowUser> newFollowers = pendingRequests.stream().map(request -> {
                    FollowUser follow = new FollowUser();
                    follow.setFollowing(user);
                    follow.setFollower(request.getSenderId());
                    return follow;
                }).collect(Collectors.toList());

                // Save all new followers and delete requests
                followRepo.saveAll(newFollowers);
                followRequestRepo.deleteAll(pendingRequests);
            }
        }
        
        return "Privacy settings updated successfully!";
    }

    // Account Deactivation Service (Soft Delete)
    public String deactivateAccount(DeactivateRequestDTO deactivateRequestDTO) {
        // Get UserId from Security Context JWT Token
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userUid = Long.parseLong(userIdStr);
        Users user = usersRepo.findByUserId(userUid)
                              .orElseThrow(() -> new IllegalArgumentException("User not found!"));

        // Verify Password
        if (deactivateRequestDTO.getPassword() == null || deactivateRequestDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required for deactivation!");
        }

        if (!passwordHashing.verifyPassword(deactivateRequestDTO.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect password!");
        }

        user.setStatusDeleted(true);
        usersRepo.save(user);
        return "Account deactivated successfully!";
    }


    // Change Password Service
    public String changePasswordService(ChangePasswordRequestDTO changePasswordRequestDTO) {
        // Get UserId from Security Context JWT Token
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userUid = Long.parseLong(userIdStr);
        Users user = usersRepo.findByUserId(userUid)
                              .orElseThrow(() -> new IllegalArgumentException("User not found!"));

        // Validate Old Password
        if (changePasswordRequestDTO.getOldPassword() == null || changePasswordRequestDTO.getOldPassword().isEmpty()) {
            throw new IllegalArgumentException("Old password is required!");
        }
        if (!passwordHashing.verifyPassword(changePasswordRequestDTO.getOldPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect old password!");
        }

        // Validate New Password
        if (changePasswordRequestDTO.getNewPassword() == null || changePasswordRequestDTO.getNewPassword().isEmpty()) {
            throw new IllegalArgumentException("New password is required!");
        }
        if (changePasswordRequestDTO.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long!");
        }

        // Update Password
        String hashedNewPassword = passwordHashing.hashPassword(changePasswordRequestDTO.getNewPassword());
        user.setPasswordHash(hashedNewPassword);
        usersRepo.save(user);
        
        return "Password changed successfully!";
    }

    // Fetching Blocked Users List Service
    @Transactional
    public List<BlockedUserFetchDTO> fetchBlockedUsersList() {
        // Get UserId from Security Context JWT Token
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userUid = Long.parseLong(userIdStr);
        Users user = usersRepo.findByUserId(userUid)
                              .orElseThrow(() -> new IllegalArgumentException("User not found!"));
        
        // Create Blocked Users List
        List<BlockedUserFetchDTO> blockedUsers = new ArrayList<>();

        // Get All Blocked User List where current user is the blocker
        List<BlockUser> blockedList = blockRepo.findByBlocker(user);

        for (BlockUser block : blockedList) {
            Users blockedUser = block.getBlocked();
            BlockedUserFetchDTO dto = new BlockedUserFetchDTO();
            dto.setUsername(blockedUser.getUsername());
            dto.setUserId(blockedUser.getUserId().toString());
            if (blockedUser.getUserData() != null) {
                dto.setProfilePicture(blockedUser.getUserData().getProfilePhoto());
            }
            blockedUsers.add(dto);
        }

        return blockedUsers;
            
    }
    
}
