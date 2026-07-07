package com.loginapp.loginapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.loginapp.loginapp.entity.SettingPreferences;
import com.loginapp.loginapp.entity.Users;

public interface SettingPreferencesRepo extends JpaRepository<SettingPreferences, Long> {
    

    // Find SettingPreferences by User
    SettingPreferences findByUser(@Param("user") Users user);
}
