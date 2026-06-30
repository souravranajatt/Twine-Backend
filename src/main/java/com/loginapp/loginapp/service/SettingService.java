package com.loginapp.loginapp.service;

import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.loginapp.loginapp.DTO.BlockedUserFetchDTO;
import com.loginapp.loginapp.DTO.ChangePasswordRequestDTO;
import com.loginapp.loginapp.DTO.DeactivateRequestDTO;
import com.loginapp.loginapp.DTO.PersonalDetailsDTO;
import com.loginapp.loginapp.DTO.SettingDataDTO;
import com.loginapp.loginapp.Utils.AuthUtils;
import com.loginapp.loginapp.Utils.CloudinaryService;
import com.loginapp.loginapp.Utils.PasswordHashing;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.entity.UserData;
import com.loginapp.loginapp.entity.AccountDeactivation;
import com.loginapp.loginapp.entity.FollowRequestTable;
import com.loginapp.loginapp.entity.FollowUser;
import com.loginapp.loginapp.repository.UsersRepo;
import com.loginapp.loginapp.repository.FollowRequestRepo;
import com.loginapp.loginapp.repository.AccountDeactivationRepo;
import com.loginapp.loginapp.repository.BlockRepo;
import com.loginapp.loginapp.repository.FollowRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SettingService {

    // Inject Other Files thorugh constructor

    private final AuthUtils authUtils;
    
    private final UsersRepo usersRepo;

    private final CloudinaryService cloudinaryService;

    private final FollowRequestRepo followRequestRepo;

    private final FollowRepo followRepo;

    private final PasswordHashing passwordHashing;

    private final BlockRepo blockRepo;

    private final AccountDeactivationRepo accountDeactivationRepo;

    // Username regex (only lowercase letters, numbers, underscore)
    private static final String USERNAME_REGEX = "^[a-z0-9_.]+$";
    private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);

    // Email regex
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    // Mobile number regex (optional +, 7 to 15 digits)
    private static final String MOBILE_REGEX = "^\\+?[0-9]{7,15}$";
    private static final Pattern MOBILE_PATTERN = Pattern.compile(MOBILE_REGEX);

    SettingService(AuthUtils authUtils, UsersRepo usersRepo, CloudinaryService cloudinaryService, FollowRequestRepo followRequestRepo, FollowRepo followRepo, PasswordHashing passwordHashing, BlockRepo blockRepo, AccountDeactivationRepo accountDeactivationRepo) {
        this.authUtils = authUtils;
        this.usersRepo = usersRepo;
        this.cloudinaryService = cloudinaryService;
        this.followRequestRepo = followRequestRepo;
        this.followRepo = followRepo;
        this.passwordHashing = passwordHashing;
        this.blockRepo = blockRepo;
        this.accountDeactivationRepo = accountDeactivationRepo;
    }

    // Profile Data Fetch Setting Service
    public SettingDataDTO settingProfileData(){

        // Get UserId from Security Context JWT Token
        Users user = authUtils.getLoggedUser();

        // Create and return SettingDataDTO
        SettingDataDTO settingDataDTO = new SettingDataDTO();
        settingDataDTO.setFullname(user.getFullname());
        settingDataDTO.setUsername(user.getUsername());
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
        Users user = authUtils.getLoggedUser();

        // Update User Data
        
        //  1. Null and Empty Checks 
        if (updateDataDTO.getUsername() == null || updateDataDTO.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required!");
        }
        if (updateDataDTO.getFullname() == null || updateDataDTO.getFullname().trim().isEmpty()) {
            throw new IllegalArgumentException("Fullname is required!");
        }

        //  2. Trim Data 
        String fullnameFinal = updateDataDTO.getFullname().trim();
        String usernameFinal = updateDataDTO.getUsername().trim().toLowerCase();

        if (fullnameFinal.length() > 30) {
            throw new IllegalArgumentException("Fullname can't exceed 30 characters!");
        }

        //  4. Username Validation 
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



        //  Update Users entity 
        user.setFullname(fullnameFinal);
        user.setUsername(usernameFinal);

        //  Handle Cloudinary Base64 Photo Upload 
        String photoStr = updateDataDTO.getProfilePictureUrl();
            if (photoStr != null && photoStr.trim().isEmpty()) {
                updateDataDTO.setProfilePictureUrl(null);
            } else if (photoStr != null && photoStr.startsWith("data:image")) {
                try {
                    String[] parts = photoStr.split(",");

                    // check format validity
                    if (parts.length < 2) {
                        throw new IllegalArgumentException("Invalid image format!");
                    }

                    String base64Data = parts[1];
                    String contentType = parts[0].split(";")[0].split(":")[1];

                    // ceheck allowed formats
                    if (!List.of(
                        "image/jpeg",
                        "image/png",
                        "image/webp",
                        "image/heic",
                        "image/heif"
                    ).contains(contentType)) {
                        throw new IllegalArgumentException("Only JPEG, PNG, WEBP, HEIC images allowed!");
                    }

                    String cleanBase64 = base64Data.replaceAll("\\s", "");
                    byte[] imageBytes = java.util.Base64.getMimeDecoder().decode(cleanBase64);

                    //  Size check — 20MB max
                    if (imageBytes.length > 20 * 1024 * 1024) {
                        throw new IllegalArgumentException("Photo too large! Max 20MB allowed.");
                    }

                    String fileName = "TWINE_PID" + user.getUserId() + "_" + System.currentTimeMillis();
                    String newPhotoUrl = cloudinaryService.uploadFile(imageBytes, fileName, contentType);
                    updateDataDTO.setProfilePictureUrl(newPhotoUrl);

                } catch (IllegalArgumentException e) {
                    throw e;
                } catch (Exception e) {
                    throw new IllegalArgumentException("Failed to upload profile photo!");
                }
            }

        //  Update or Create UserData entity 
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


    // Update Privacy Status Service (Private/Public)
    public String updatePrivacyPrivateStatus(boolean isPrivate) {
        // Get UserId from Security Context JWT Token
        Users user = authUtils.getLoggedUser();

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
        Users user = authUtils.getLoggedUser();

        // Verify Password
        if (deactivateRequestDTO.getPassword() == null || deactivateRequestDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required for deactivation!");
        }

        if (!passwordHashing.verifyPassword(deactivateRequestDTO.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect password!");
        }

        // Already deactivated check
        if (user.isStatusDeleted()) {
            throw new IllegalArgumentException("Account is already deactivated!");
        }


        // Insert Deactivate Data 
        AccountDeactivation newData = new AccountDeactivation();
        newData.setReason(deactivateRequestDTO.getReason());
        newData.setUser(user);
        accountDeactivationRepo.save(newData);


        user.setStatusDeleted(true);
        usersRepo.save(user);
        return "Account deactivated successfully!";
    }


    // Change Password Service
    public String changePasswordService(ChangePasswordRequestDTO changePasswordRequestDTO) {
        // Get UserId from Security Context JWT Token
        Users user = authUtils.getLoggedUser();

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
    public List<BlockedUserFetchDTO> fetchBlockedUsersList() {
        // Get UserId from Security Context JWT Token
        Users user = authUtils.getLoggedUser();

        // Create Blocked Users List
        List<BlockedUserFetchDTO> blockedUsers = new ArrayList<>();

        // Get All Blocked User List where current user is the blocker
        List<Users> blockedList = blockRepo.findActiveBlockedUsers(user);

        for (Users block : blockedList) {
            BlockedUserFetchDTO dto = new BlockedUserFetchDTO();
            dto.setUsername(block.getUsername());
            dto.setUserId(block.getUserId().toString());
            if (block.getUserData() != null) {
                dto.setProfilePicture(block.getUserData().getProfilePhoto());
            }
            blockedUsers.add(dto);
        }

        return blockedUsers;
            
    }

    // User Personal Details Fetch Logic ..
    public PersonalDetailsDTO personalDetailsFetch() {

        // Get UserId from Security Context JWT Token
        Users user = authUtils.getLoggedUser();

        PersonalDetailsDTO detailsDTO = new PersonalDetailsDTO();
        detailsDTO.setEmailId(user.getEmail());
        detailsDTO.setMobileNumber(user.getMobileNumber());
        return detailsDTO;
    }

    public String personalDetailsUpdate(PersonalDetailsDTO personalDetailsDTO) {

        // Get UserId from Security Context JWT Token
        Users user = authUtils.getLoggedUser();

        // if null or empty throw error 
        boolean emailEmpty = personalDetailsDTO.getEmailId() == null || 
                            personalDetailsDTO.getEmailId().trim().isEmpty();
        boolean mobileEmpty = personalDetailsDTO.getMobileNumber() == null || 
                            personalDetailsDTO.getMobileNumber().trim().isEmpty();

        if (emailEmpty) {
            throw new IllegalArgumentException("Please provide email!");
        }
        if (mobileEmpty) {
            throw new IllegalArgumentException("Please provide mobile number!");
        }

        // Validate and Update Email
        String newEmail = personalDetailsDTO.getEmailId();
        if (newEmail != null && !newEmail.trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(newEmail).matches()) {
                throw new IllegalArgumentException("Invalid email format!");
            }
            if (!user.getEmail().equals(newEmail) && usersRepo.findByEmail(newEmail).isPresent()) {
                throw new IllegalArgumentException("Email already in use!");
            }
            user.setEmail(newEmail);
        }

        // Validate and Update Mobile Number
        String newMobile = personalDetailsDTO.getMobileNumber();
        if (newMobile != null && !newMobile.trim().isEmpty()) {
            if (!MOBILE_PATTERN.matcher(newMobile).matches()) {
                throw new IllegalArgumentException("Invalid mobile number format!");
            }
            if (!newMobile.equals(user.getMobileNumber()) && usersRepo.findByMobileNumber(newMobile).isPresent()) {
                throw new IllegalArgumentException("Mobile number already in use!");
            }
            user.setMobileNumber(newMobile);
        }

        usersRepo.save(user);
        return "Personal details updated successfully!";
    }
    
}
