package com.loginapp.loginapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loginapp.loginapp.DTO.SettingDataDTO;
import com.loginapp.loginapp.service.SettingService;

@RestController
@RequestMapping("/api")
public class SettingController {
    
    @Autowired
    private SettingService settingService;

    // Logged User Profile Fetch End Point
    @GetMapping("/profile/data/setting")    
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
}
