package com.loginapp.loginapp.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.loginapp.loginapp.entity.PostSeen;
import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.Users;


public interface PostSeenRepo extends JpaRepository<PostSeen, Long> {

    PostSeen findByPostAndUser(PostsEntity postId, Users userId);

    // Collect seen post for Home Feed
    @Query("""
            SELECT ps.post.postId FROM PostSeen ps
            WHERE ps.user = :user
            """)
    Set<Long> findSeenPostIdsByUser(Users user);

    // Delete on a specific post
    @Modifying
    @Query("""
            DELETE FROM PostSeen ps 
            WHERE ps.post = :post
            """)
    void deleteForSpecificPost(@Param("post") PostsEntity post);
}
