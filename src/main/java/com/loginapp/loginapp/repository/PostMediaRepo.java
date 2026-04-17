package com.loginapp.loginapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.loginapp.loginapp.entity.PostMedia;
import com.loginapp.loginapp.entity.PostsEntity;
import java.util.Optional;


public interface PostMediaRepo extends JpaRepository<PostMedia, Long>{
    Optional<PostMedia> findByPost(PostsEntity post);

    @Query("""
            SELECT pm FROM PostMedia pm 
            WHERE pm.post = :post
            """)
    PostMedia findPostMedia(@Param("post") PostsEntity post);
}
