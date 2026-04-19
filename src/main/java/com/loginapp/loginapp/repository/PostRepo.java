package com.loginapp.loginapp.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.*;
import com.loginapp.loginapp.entity.Users;

import com.loginapp.loginapp.entity.PostsEntity;

public interface PostRepo extends JpaRepository<PostsEntity, Long> {
    Long countByUserpost_UserId(Long userId);

    @Query("""
            SELECT p FROM PostsEntity p
            WHERE p.userpost=:user
            AND p.postVisiblity = true
            ORDER BY p.uploadAt DESC
            """)
    List<PostsEntity> findUserPosts(Users user, Pageable page);
}
