package com.loginapp.loginapp.repository;

import java.util.*;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.SavedPosts;
import com.loginapp.loginapp.entity.Users;


public interface SavedPostRepo extends JpaRepository<SavedPosts, Long> {

    boolean existsByUserAndPost(Users user, PostsEntity post);
    
    // Saved Post Fetching for a specific user and post
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

    // Find all Saved Posts for a specific user
    @Query("""
        SELECT sp.post FROM SavedPosts sp
        JOIN FETCH sp.post.userpost
        LEFT JOIN FETCH sp.post.postMedia
        WHERE sp.user = :user
        AND sp.post.postVisiblity = true
        AND sp.post.userpost.statusDeleted = false
        ORDER BY sp.savedAt DESC
        """)
    List<PostsEntity> findSavedPostsByUser(@Param("user") Users user, Pageable pageable);

}
