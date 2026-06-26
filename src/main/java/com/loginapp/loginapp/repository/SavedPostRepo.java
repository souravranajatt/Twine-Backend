package com.loginapp.loginapp.repository;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.SavedPosts;
import com.loginapp.loginapp.entity.Users;


public interface SavedPostRepo extends JpaRepository<SavedPosts, Long> {

    boolean existsByUserAndPost(Users user, PostsEntity post);
    
    @Query("SELECT sp.post.postId FROM SavedPosts sp WHERE sp.user = :user AND sp.post.postId IN :postIds")
    Set<Long> findSavedPostIdsByUserAndPostIds(@Param("user") Users user, @Param("postIds") List<Long> postIds);

    Optional<SavedPosts> findByUserAndPost(Users user, PostsEntity post);

    // Delete on a specific post 
    @Modifying
    @Query("""
            DELETE FROM SavedPosts sp
            WHERE sp.post = :post
            """)
    void deleteForSpecificPost(@Param("post") PostsEntity post);
}
