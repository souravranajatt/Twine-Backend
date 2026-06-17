package com.loginapp.loginapp.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.Users;

@Repository
public interface HomeFeedRepo extends JpaRepository<PostsEntity, Long> {

    // Interest Based Posts
    @Query("""
        SELECT p FROM PostsEntity p
        LEFT JOIN FETCH p.postMedia
        JOIN FETCH p.userpost
        JOIN PostCategories pc ON pc.post = p
        WHERE pc.primaryCategory = :category
        AND p.postVisiblity = true
        AND p.userpost.statusPrivate = false
        AND p.userpost.statusDeleted = false
        AND p.uploadAt >= :cutoffDate
        AND p.postId NOT IN :seenPostIds
        ORDER BY (
            p.viewCount * 1 +
            p.likeCount * 2 +
            p.saveCount * 3 +
            CASE
                WHEN p.uploadAt >= :recentCutoff THEN 20
                ELSE 0
            END
        ) DESC
    """)
    List<PostsEntity> getPostsByCategory(
        @Param("category") String category,
        @Param("cutoffDate") LocalDateTime cutoffDate,
        @Param("recentCutoff") LocalDateTime recentCutoff,
        @Param("seenPostIds") Set<Long> seenPostIds,
        Pageable pageable
    );

    // Following List Posts
    @Query("""
        SELECT p FROM PostsEntity p
        LEFT JOIN FETCH p.postMedia
        JOIN FETCH p.userpost
        WHERE p.userpost IN :users
        AND p.postVisiblity = true
        AND p.userpost.statusDeleted = false
        AND p.postId NOT IN :seenPostIds
        ORDER BY p.uploadAt DESC
    """)
    List<PostsEntity> getFollowingPosts(
        @Param("users") List<Users> users,
        @Param("seenPostIds") Set<Long> seenPostIds,
        Pageable pageable
    );

    // Trending Posts
    @Query("""
        SELECT p FROM PostsEntity p
        LEFT JOIN FETCH p.postMedia
        JOIN FETCH p.userpost
        WHERE p.postVisiblity = true
        AND p.userpost.statusPrivate = false
        AND p.userpost.statusDeleted = false
        AND p.uploadAt >= :cutoffDate
        ORDER BY (
            p.likeCount * 3 +
            p.commentCount * 5 +
            p.saveCount * 4 +
            p.viewCount * 1 +
            CASE
                WHEN p.uploadAt >= :recentCutoff THEN 20
                ELSE 0
            END
        ) DESC
    """)
    List<PostsEntity> getTrendingPosts(
        @Param("cutoffDate") LocalDateTime cutoffDate,
        @Param("recentCutoff") LocalDateTime recentCutoff,
        Pageable pageable
    );

    // Posts for newly created account 
    @Query("""
        SELECT p FROM PostsEntity p
        LEFT JOIN FETCH p.postMedia
        JOIN FETCH p.userpost
        WHERE p.postVisiblity = true
        AND p.userpost.statusPrivate = false
        AND p.userpost.statusDeleted = false
        AND p.postId NOT IN :seenPostIds
        ORDER BY (
            p.likeCount * 3 +
            p.commentCount * 5 +
            p.saveCount * 4 +
            p.viewCount * 1
        ) DESC
    """)
    List<PostsEntity> getFallbackPosts(
        @Param("seenPostIds") Set<Long> seenPostIds,
        Pageable pageable
    );
}