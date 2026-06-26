package com.loginapp.loginapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loginapp.loginapp.DTO.BlockedUserFetchDTO;
import com.loginapp.loginapp.DTO.ChangePasswordRequestDTO;
import com.loginapp.loginapp.DTO.DeactivateRequestDTO;
import com.loginapp.loginapp.DTO.PersonalDetailsDTO;
import com.loginapp.loginapp.DTO.SettingDataDTO;
import com.loginapp.loginapp.service.SettingService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;




@RestController
@RequestMapping("/api")
public class SettingController {
    
    private final SettingService settingService;

    SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    // Logged User Profile Fetch End Point
    @GetMapping("/setting/profile/fetch")    
    public ResponseEntity<?> profileDataSetting(){
        try{
            SettingDataDTO settingDataDTO = settingService.settingProfileData();
            return ResponseEntity.ok(settingDataDTO);
        }catch(IllegalArgumentException err){
            return ResponseEntity.badRequest().body(err.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Logged User Profile Update End Point
    @PutMapping("/setting/profile/update")
    public ResponseEntity<?> profileDataUpdateSetting(@RequestBody SettingDataDTO updateDataDTO){
        try{
            String result = settingService.settingProfileDataUpdate(updateDataDTO);
            return ResponseEntity.ok(result);
        }catch(IllegalArgumentException err){
            return ResponseEntity.badRequest().body(err.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Private Account Update End Point
    @PatchMapping("/setting/privacy/private/update")
    public ResponseEntity<?> profilePrivacyPrivateUpdateSetting(@RequestBody Boolean isPrivate){
        try{
            String result = settingService.updatePrivacyPrivateStatus(isPrivate);
            return ResponseEntity.ok(result);
        }catch(IllegalArgumentException err){
            return ResponseEntity.badRequest().body(err.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Account Deactivation End Point
    @PatchMapping("/setting/account/deactivate")
    public ResponseEntity<?> accountDeactivationSetting(@RequestBody DeactivateRequestDTO deactivateRequestDTO){
        try{
            String result = settingService.deactivateAccount(deactivateRequestDTO);
            return ResponseEntity.ok(result);
        }catch(IllegalArgumentException err){
            return ResponseEntity.badRequest().body(err.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Password Change End Point
    @PutMapping("/setting/password/update")
    public ResponseEntity<?> changePasswordSetting(@RequestBody ChangePasswordRequestDTO changePasswordRequestDTO){
        try{
            String result = settingService.changePasswordService(changePasswordRequestDTO);
            return ResponseEntity.ok(result);
        }catch(IllegalArgumentException err){
            return ResponseEntity.badRequest().body(err.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Block User End Point
    @GetMapping("/setting/block/list/fetch")
    public ResponseEntity<?> getMethodName() {
        try{
            List<BlockedUserFetchDTO> blockedUsers = settingService.fetchBlockedUsersList();
            return ResponseEntity.ok(blockedUsers);
        }catch(IllegalArgumentException err){
            return ResponseEntity.badRequest().body(err.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
    
    // Personal Details Fetch End Point
    @GetMapping("/setting/personal/details/fetch")
    public ResponseEntity<?> getPersonalDetails() {
        try{
            PersonalDetailsDTO detailsDTO = settingService.personalDetailsFetch();
            return ResponseEntity.ok(detailsDTO);
        }catch(IllegalArgumentException err){
            return ResponseEntity.badRequest().body(err.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Personal Details Update End Point
    @PutMapping("/setting/personal/details/update")
    public ResponseEntity<?> updatePersonalDetails(@RequestBody PersonalDetailsDTO personalDetailsDTO) {
        try{
            String result = settingService.personalDetailsUpdate(personalDetailsDTO);
            return ResponseEntity.ok(result);
        }catch(IllegalArgumentException err){
            return ResponseEntity.badRequest().body(err.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("Internal server error");    
        }
    }

    
}
