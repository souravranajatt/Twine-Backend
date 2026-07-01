package com.loginapp.loginapp.repository;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.loginapp.loginapp.entity.PostLike;
import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.Users;

public interface PostLikeRepo extends JpaRepository<PostLike, Long> {

    boolean existsByPostAndUser(PostsEntity post, Users loggedUser);

    // Liked Post Ids for a specific user
    @Query("SELECT pl.post.postId FROM PostLike pl WHERE pl.user = :user AND pl.post.postId IN :postIds")
    Set<Long> findLikedPostIdsByUserAndPostIds(@Param("user") Users user, @Param("postIds") List<Long> postIds);

    Optional<PostLike> findByPostAndUser(PostsEntity post, Users loggedUser);

    // Delete on a specific post
    @Modifying
    @Query("""
            DELETE FROM PostLike pl 
            WHERE pl.post = :post
            """)
    void deleteForSpecificPost(@Param("post") PostsEntity post);
    
}
