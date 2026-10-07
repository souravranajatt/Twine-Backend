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

    // Atomic Reply Count Queries (Prevents Race Conditions)
    @Modifying
    @Query("UPDATE PostComment c SET c.replyCount = c.replyCount + 1 WHERE c.commentId = :commentId")
    void incrementReplyCount(@Param("commentId") Long commentId);

    @Modifying
    @Query("UPDATE PostComment c SET c.replyCount = CASE WHEN c.replyCount > 0 THEN c.replyCount - 1 ELSE 0 END WHERE c.commentId = :commentId")
    void decrementReplyCount(@Param("commentId") Long commentId);
}
