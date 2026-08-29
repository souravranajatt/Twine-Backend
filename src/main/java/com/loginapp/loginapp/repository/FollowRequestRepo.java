package com.loginapp.loginapp.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.loginapp.loginapp.entity.FollowRequestTable;
import com.loginapp.loginapp.entity.Users;

public interface FollowRequestRepo extends JpaRepository<FollowRequestTable, Long> {

    // Find follow request between a specific sender and receiver
    Optional<FollowRequestTable> findBySenderIdAndReceiverId(Users sender, Users receiver);

    // Check if a follow request exists between sender and receiver
    Boolean existsBySenderIdAndReceiverId(Users sender, Users receiver);

    // Fetch all follow requests received by a user (active senders only)
    @Query("""
        SELECT r FROM FollowRequestTable r
        JOIN FETCH r.senderId
        WHERE r.receiverId = :receiver
        AND r.senderId.statusDeleted = false
        ORDER BY r.requestedOn DESC
    """)
    List<FollowRequestTable> findByReceiverId(@Param("receiver") Users receiver);

    // Get all user IDs to whom the sender has sent follow requests
    @Query("SELECT r.receiverId.userId FROM FollowRequestTable r WHERE r.senderId = :sender")
    Set<Long> findSentFollowRequestReceiverIds(@Param("sender") Users sender);

    // Delete follow request between a specific sender and receiver
    void deleteBySenderIdAndReceiverId(Users sender, Users receiver);

    // Fetch paginated incoming follow requests for a specific user
    @Query("""
        SELECT r FROM FollowRequestTable r
        JOIN FETCH r.senderId
        WHERE r.receiverId = :receiver
        AND r.senderId.statusDeleted = false
        AND r.senderId.statusSuspend = false
        ORDER BY r.requestedOn DESC
    """)
    List<FollowRequestTable> findFollowRequestsForUser(@Param("receiver") Users receiver, Pageable pageable);

}
