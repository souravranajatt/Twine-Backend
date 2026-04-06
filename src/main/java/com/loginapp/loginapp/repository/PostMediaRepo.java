package com.loginapp.loginapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loginapp.loginapp.entity.PostMedia;
import com.loginapp.loginapp.entity.PostsEntity;
import java.util.Optional;


public interface PostMediaRepo extends JpaRepository<PostMedia, Long>{
    Optional<PostMedia> findByPost(PostsEntity post);
}
