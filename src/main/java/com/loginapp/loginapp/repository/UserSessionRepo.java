package com.loginapp.loginapp.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.loginapp.loginapp.entity.UserSession;

public interface UserSessionRepo extends CrudRepository<UserSession, String> {

    List<UserSession> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
