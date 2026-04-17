package com.loginapp.loginapp.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.Users;

@Repository
public interface HomeFeedRepo extends JpaRepository<PostsEntity, Long>{
    
    // 🔹 Interest Based
    @Query("""
        SELECT p FROM PostsEntity p
        JOIN PostCategories pc ON pc.post = p
        WHERE pc.primaryCategory = :category
        AND p.postVisiblity = true
        ORDER BY p.uploadAt DESC
    """)
    List<PostsEntity> getPostsByCategory(String category, Pageable pageable);

    // 🔹 Following Feed (FIXED 🔥)
    @Query("""
        SELECT p FROM PostsEntity p
        WHERE p.userpost IN :user
        AND p.postVisiblity = true
        ORDER BY p.uploadAt DESC
    """)
    List<PostsEntity> getFollowingPosts(List<Users> user, Pageable pageable);

    // 🔹 Trending
    @Query("""
        SELECT p FROM PostsEntity p
        WHERE p.postVisiblity = true
        ORDER BY (p.likeCount + p.viewCount) DESC
    """)
    List<PostsEntity> getTrendingPosts(Pageable pageable);

    // 🔹 Recent
    @Query("""
        SELECT p FROM PostsEntity p
        WHERE p.postVisiblity = true
        ORDER BY p.uploadAt DESC
    """)
    List<PostsEntity> getRecentPosts(Pageable pageable);
}
