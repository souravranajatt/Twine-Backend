package com.loginapp.loginapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loginapp.loginapp.entity.PostCategories;
import com.loginapp.loginapp.entity.PostsEntity;

@Repository
public interface PostCategoryRepo extends JpaRepository<PostCategories, Long>{
    
    Optional<PostCategories> findByPost(PostsEntity post);
    
    Optional<PostCategories> findByPostPostId(Long postId);
}
