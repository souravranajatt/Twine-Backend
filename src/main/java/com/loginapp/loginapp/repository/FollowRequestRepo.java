package com.loginapp.loginapp.repository;

import java.util.Optional;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.loginapp.loginapp.entity.FollowRequestTable;
import com.loginapp.loginapp.entity.Users;

public interface FollowRequestRepo extends JpaRepository<FollowRequestTable, Long>{
    
    Optional<FollowRequestTable> findBySenderIdAndReceiverId(Users sender, Users receiver);

    Boolean existsBySenderIdAndReceiverId(Users sender, Users receiver);

    @Query("""
        SELECT r FROM FollowRequestTable r
        JOIN FETCH r.senderId
        WHERE r.receiverId = :receiver
        AND r.senderId.statusDeleted = false
        ORDER BY r.requestedOn DESC
    """)
    List<FollowRequestTable> findByReceiverId(@Param("receiver") Users receiver);

    @Query("SELECT r.receiverId.userId FROM FollowRequestTable r WHERE r.senderId = :sender")
    Set<Long> findSentFollowRequestReceiverIds(@Param("sender") Users sender);

    void deleteBySenderIdAndReceiverId(Users sender, Users receiver);

}
