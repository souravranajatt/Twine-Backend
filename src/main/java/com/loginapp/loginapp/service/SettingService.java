package com.loginapp.loginapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.loginapp.loginapp.DTO.SettingDataDTO;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.UsersRepo;

@Service
public class SettingService {
    
    @Autowired
    private UsersRepo usersRepo;

    // Profile Data Setting Service
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
    
}
