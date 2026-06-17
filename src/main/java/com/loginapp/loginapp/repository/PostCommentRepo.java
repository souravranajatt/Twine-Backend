package com.loginapp.loginapp.repository;


import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.loginapp.loginapp.entity.PostComment;

@Repository
public interface PostCommentRepo extends JpaRepository<PostComment, Long> {

    // Fetch all comment where parent id is null
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

}
