package com.loginapp.loginapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loginapp.loginapp.DTO.ArchivePostsDTO;
import com.loginapp.loginapp.DTO.BlockedUserFetchDTO;
import com.loginapp.loginapp.DTO.ChangePasswordRequestDTO;
import com.loginapp.loginapp.DTO.DeactivateRequestDTO;
import com.loginapp.loginapp.DTO.PersonalDetailsDTO;
import com.loginapp.loginapp.DTO.PostFetchDTO;
import com.loginapp.loginapp.DTO.SettingDataDTO;
import com.loginapp.loginapp.DTO.SettingIntreactionDTO;
import com.loginapp.loginapp.service.SettingService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;





@RestController
@RequestMapping("/api/setting")
public class SettingController {
    
    private final SettingService settingService;

    SettingController(SettingService settingService) {
        this.settingService = settingService;
    }



    // ***************** Account Controllers ********************



    // Logged User Profile Fetch End Point
    @GetMapping("/account/profile-fetch")    
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
    @PutMapping("/account/profile-update")
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

    // Account Deactivation End Point
    @PatchMapping("/account/deactivate")
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


    // Personal Details Fetch End Point
    @GetMapping("/account/personal-details-fetch")
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
    @PutMapping("/account/personal-details-update")
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





    // ***************** Privacy Controllers ********************




    // Private Account Update End Point
    @PatchMapping("/privacy/private-account")
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

    // Block User End Point
    @GetMapping("/privacy/block-list")
    public ResponseEntity<?> fetchBlockList() {
        try{
            List<BlockedUserFetchDTO> blockedUsers = settingService.fetchBlockedUsersList();
            return ResponseEntity.ok(blockedUsers);
        }catch(IllegalArgumentException err){
            return ResponseEntity.badRequest().body(err.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Fetch Intreaction Settings End Point
    @GetMapping("/privacy/intreaction-settings")
    public ResponseEntity<?> fetchIntreactionSettings() {
        try{
            SettingIntreactionDTO intreactionSettings = settingService.fetchIntreactionPreferences();
            return ResponseEntity.ok(intreactionSettings);
        }catch(IllegalArgumentException err){
            return ResponseEntity.badRequest().body(err.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Hide Like by default on new posts End Point
    @PatchMapping("/privacy/hide-like")
    public ResponseEntity<?> hideLikeDefaultSetting() {
        try{
            String result = settingService.hideLikeDefaultSetting();
            return ResponseEntity.ok(result);
        }catch(IllegalArgumentException err){
            return ResponseEntity.badRequest().body(err.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Show Like by default on new posts End Point
    @PatchMapping("/privacy/show-like")
    public ResponseEntity<?> showLikeDefaultSetting() {
        try{
            String result = settingService.showLikeDefaultSetting();
            return ResponseEntity.ok(result);
        }catch(IllegalArgumentException err){
            return ResponseEntity.badRequest().body(err.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Turn Off Commenting by default on new posts End Point
    @PatchMapping("/privacy/turn-off-commenting")
    public ResponseEntity<?> turnOffCommentingDefaultSetting() {
        try{
            String result = settingService.turnOffCommentingDefaultSetting();
            return ResponseEntity.ok(result);
        }catch(IllegalArgumentException err){
            return ResponseEntity.badRequest().body(err.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // Turn On Commenting by default on new posts End Point
    @PatchMapping("/privacy/turn-on-commenting")
    public ResponseEntity<?> turnOnCommentingDefaultSetting() {
        try{
            String result = settingService.turnOnCommentingDefaultSetting();
            return ResponseEntity.ok(result);
        }catch(IllegalArgumentException err){
            return ResponseEntity.badRequest().body(err.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(500).body("Internal server error");
        }
    }


    // ***************** Security Controllers ********************



    // Password Change End Point
    @PutMapping("/security/password-change")
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




    // ***************** Your Activity Controllers ********************

    // 1. Saved Posts Fetch End Point
    @GetMapping("/activity/saved-posts")
    public ResponseEntity<?> getSavedPosts(@RequestParam(defaultValue = "0") int page) {
        try{
            List<PostFetchDTO> savedPosts = settingService.fetchSavedPosts(page);
            return ResponseEntity.ok(savedPosts);
        } catch (IllegalArgumentException err) {
            return ResponseEntity.badRequest().body(err.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // 2. Archived Posts Fetch End Point
    @GetMapping("/activity/archive-posts")
    public ResponseEntity<?> getArchivedPosts(@RequestParam(defaultValue = "0") int page) {
        try{
            List<ArchivePostsDTO> archivedPosts = settingService.fetchArchivedPosts(page);
            return ResponseEntity.ok(archivedPosts);
        } catch (IllegalArgumentException err) {
            return ResponseEntity.badRequest().body(err.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
    

    
}
