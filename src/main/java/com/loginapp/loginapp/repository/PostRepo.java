package com.loginapp.loginapp.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.loginapp.loginapp.entity.Users;

import com.loginapp.loginapp.entity.PostsEntity;
import java.util.List;


public interface PostRepo extends JpaRepository<PostsEntity, Long> {
        
        @Query("""
                SELECT COUNT(p) FROM PostsEntity p
                WHERE p.userpost.userId = :userId
                AND p.postVisiblity = true
                AND p.userpost.statusDeleted = false
        """)
        Long countByUserpost_UserId(@Param("userId") Long userId);

        @Query("""
                SELECT p FROM PostsEntity p
                LEFT JOIN FETCH p.postMedia
                JOIN FETCH p.userpost
                WHERE p.userpost=:user
                AND p.postVisiblity = true
                And p.userpost.statusDeleted = false
                ORDER BY p.uploadAt DESC
                """)
        List<PostsEntity> findUserPosts(Users user, Pageable page);

        @Query("""
                SELECT p FROM PostsEntity p
                LEFT JOIN FETCH p.postMedia
                JOIN FETCH p.userpost
                WHERE p.timelineUser=:userid
                AND p.userpost.userId = :tuserid
                AND p.postVisiblity = true
                AND p.userpost.statusDeleted = false
                ORDER BY p.uploadAt DESC
                """)
        List<PostsEntity> findTimelinePosts(@Param("tuserid") Long tuserid, @Param("userid") Long userid, Pageable page);

        @Query("""
                SELECT p FROM PostsEntity p
                LEFT JOIN FETCH p.postMedia
                JOIN FETCH p.userpost
                WHERE :userId MEMBER OF p.taggedUsers
                AND p.postVisiblity = true
                AND p.userpost.statusDeleted = false
                ORDER BY p.uploadAt DESC
                """)
        List<PostsEntity> findTaggedPosts(@Param("userId") String userId, Pageable page);

        @Query("""
                SELECT p FROM PostsEntity p
                JOIN FETCH p.userpost
                LEFT JOIN FETCH p.postMedia
                WHERE p.postId = :postId
                AND p.userpost.statusDeleted = false
                AND p.postVisiblity = true
                        """)
        PostsEntity findSpecificPost(@Param("postId") Long postId);

        @Query("""
                SELECT p FROM PostsEntity p
                JOIN FETCH p.userpost
                WHERE p.postId = :postId
                AND p.userpost.statusDeleted = false
                AND p.postVisiblity = true
                        """)
        PostsEntity findActivePost(@Param("postId") Long postId);

        // Fetch Archive Posts
        @Query("""
                SELECT p FROM PostsEntity p
                WHERE p.userpost=:user
                AND p.postVisiblity = false
                AND p.userpost.statusDeleted = false
                ORDER BY p.uploadAt DESC
                """)
        List<PostsEntity> findArchivedPostsByUser(@Param("user") Users user, Pageable page);
        
        // Fetch Archived Post
        @Query("""
                SELECT p FROM PostsEntity p
                JOIN p.userpost u
                WHERE p.postId = :postId
                AND u = :user
                AND u.statusDeleted = false
                AND p.postVisiblity = false
                        """)
        PostsEntity findArchivedPostById(@Param("postId") Long postId, @Param("user") Users user);
}