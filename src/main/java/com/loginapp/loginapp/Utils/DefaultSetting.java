package com.loginapp.loginapp.Utils;

import org.springframework.stereotype.Service;

import com.loginapp.loginapp.entity.SettingPreferences;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.SettingPreferencesRepo;

@Service
public class DefaultSetting {

    private final AuthUtils authUtils;

    private final SettingPreferencesRepo settingPreferencesRepo;

    DefaultSetting(AuthUtils authUtils, SettingPreferencesRepo settingPreferencesRepo) {
        this.authUtils = authUtils;
        this.settingPreferencesRepo = settingPreferencesRepo;
    }
    
    // Create Default SettingPreferences object with default values
    public SettingPreferences createDefaultSettingPreferences() {

        // Get User
        Users user = authUtils.getLoggedUser();
        
        SettingPreferences settingPreferences = new SettingPreferences();
        settingPreferences.setUser(user);
        settingPreferences.setCommentingEnable(true);
        settingPreferences.setLikeVisible(true);
        settingPreferences.setTaggingEnable(SettingPreferences.PreferenceVisibility.EVERYONE);
        settingPreferences.setMentionEnable(SettingPreferences.PreferenceVisibility.EVERYONE);
        settingPreferences.setDiscoverable(true);
        settingPreferencesRepo.save(settingPreferences);
        return settingPreferences;
    }
}
