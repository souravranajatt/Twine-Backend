package com.loginapp.loginapp.repository;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.loginapp.loginapp.entity.PostLike;
import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.Users;

public interface PostLikeRepo extends JpaRepository<PostLike, Long> {

    boolean existsByPostAndUser(PostsEntity post, Users loggedUser);

    @Query("SELECT pl.post.postId FROM PostLike pl WHERE pl.user = :user AND pl.post.postId IN :postIds")
    Set<Long> findLikedPostIdsByUserAndPostIds(@Param("user") Users user, @Param("postIds") List<Long> postIds);
    
}
