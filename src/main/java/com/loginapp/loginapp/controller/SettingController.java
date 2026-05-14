package com.loginapp.loginapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loginapp.loginapp.DTO.DeactivateRequestDTO;
import com.loginapp.loginapp.DTO.SettingDataDTO;
import com.loginapp.loginapp.service.SettingService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api")
public class SettingController {
    
    @Autowired
    private SettingService settingService;

    // Logged User Profile Fetch End Point
    @GetMapping("/profile/setting/fetch")    
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
    @PutMapping("/profile/setting/update")
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

    // Privacy Status Update End Point
    @PutMapping("/profile/setting/privacy/private/update")
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
    @PutMapping("/profile/setting/account/deactivate")
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

    
}
