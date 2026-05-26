package com.loginapp.loginapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loginapp.loginapp.entity.SecretCrushRequest;
import com.loginapp.loginapp.entity.Users;

public interface SecretCrushRequestRepo extends JpaRepository<SecretCrushRequest, Long>{
    boolean existsBySenderIdAndAnonymousId(Users senderId, Users anonymousId);
    
    void deleteBySenderIdAndAnonymousId(Users senderId, Users anonymousId);
}
