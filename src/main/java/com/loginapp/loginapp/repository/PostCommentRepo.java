package com.loginapp.loginapp.repository;


import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.loginapp.loginapp.entity.PostComment;
import com.loginapp.loginapp.entity.PostsEntity;

@Repository
public interface PostCommentRepo extends JpaRepository<PostComment, Long> {

    // Fetch all comment of a post where parent id is null
    @Query("""
        SELECT c FROM PostComment c
        JOIN FETCH c.user
        WHERE c.post.postId = :postId
        AND c.user.statusDeleted = false
        AND c.parentId IS NULL
        AND c.isDeleted = false
        ORDER BY c.createdAt DESC
    """)
    List<PostComment> findCommentsByPost(
        @Param("postId") Long postId,
        Pageable pageable
    );

    // Delete on a specific post
    @Modifying
    @Query("""
            DELETE FROM PostComment pc
            WHERE pc.post = :post
            """)
    void deleteForSpecificPost(@Param("post") PostsEntity post);
}
