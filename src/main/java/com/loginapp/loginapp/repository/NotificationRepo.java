package com.loginapp.loginapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.loginapp.loginapp.entity.Notification;
import com.loginapp.loginapp.entity.Notification.NotificationType;
import com.loginapp.loginapp.entity.Users;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {

    // Find existing unread notification for aggregation (e.g. existing like on same post)
    Optional<Notification> findFirstByRecipientAndTypeAndTargetIdAndIsReadFalse(
        Users recipient, NotificationType type, Long targetId
    );

    // Fetch notifications with actor and actor's userData eagerly fetched for high performance
    @Query("""
        SELECT n FROM Notification n
        JOIN FETCH n.actor a
        LEFT JOIN FETCH a.userData
        WHERE n.recipient = :recipient
        ORDER BY n.updatedAt DESC
    """)
    List<Notification> findFeedByRecipient(
        @Param("recipient") Users recipient,
        Pageable pageable
    );

    // Fast count of unread notifications
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :recipient AND n.isRead = false")
    long countUnreadByRecipient(@Param("recipient") Users recipient);

    // Mark single notification as read
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.notificationId = :id AND n.recipient = :recipient")
    int markAsRead(@Param("id") Long id, @Param("recipient") Users recipient);

    // Mark all notifications as read for a recipient
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient = :recipient AND n.isRead = false")
    int markAllAsRead(@Param("recipient") Users recipient);

    // Delete notification (e.g., Unlike)
    @Modifying
    @Query("""
        DELETE FROM Notification n
        WHERE n.recipient = :recipient
        AND n.actor = :actor
        AND n.type = :type
        AND n.targetId = :targetId
    """)
    void deleteByRecipientAndActorAndTypeAndTargetId(
        @Param("recipient") Users recipient,
        @Param("actor") Users actor,
        @Param("type") NotificationType type,
        @Param("targetId") Long targetId
    );

    // Delete notification (e.g., cancel follow request)
    @Modifying
    @Query("""
        DELETE FROM Notification n
        WHERE n.recipient = :recipient
        AND n.actor = :actor
        AND n.type = :type
    """)
    void deleteByRecipientAndActorAndType(
        @Param("recipient") Users recipient,
        @Param("actor") Users actor,
        @Param("type") NotificationType type
    );
}
