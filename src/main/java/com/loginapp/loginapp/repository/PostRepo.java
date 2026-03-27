package com.loginapp.loginapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.loginapp.loginapp.entity.PostsEntity;

public interface PostRepo extends JpaRepository<PostsEntity, Long> {
    Long countByUserpost_UserId(Long userId);
}
