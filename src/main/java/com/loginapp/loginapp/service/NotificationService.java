package com.loginapp.loginapp.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.loginapp.loginapp.DTO.NotificationDTO;
import com.loginapp.loginapp.Utils.AuthUtils;
import com.loginapp.loginapp.Utils.SocialFilterHelper;
import com.loginapp.loginapp.entity.Notification;
import com.loginapp.loginapp.entity.Notification.NotificationType;
import com.loginapp.loginapp.entity.PostComment;
import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.NotificationRepo;

import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
public class NotificationService {

    private static final ZoneId ZONE_KOLKATA = ZoneId.of("Asia/Kolkata");
    private static final String REDIS_UNREAD_PREFIX = "notif:unread:";

    private final NotificationRepo notificationRepo;
    private final SocialFilterHelper socialFilterHelper;
    private final RedisService redisService;
    private final AuthUtils authUtils;
    private final SimpMessagingTemplate messagingTemplate;

    // SSE connections per user — browser fallback when WebSocket isn't available
    private final Map<Long, List<SseEmitter>> sseEmitters = new ConcurrentHashMap<>();

    public NotificationService(NotificationRepo notificationRepo,
                               SocialFilterHelper socialFilterHelper,
                               RedisService redisService,
                               AuthUtils authUtils,
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepo = notificationRepo;
        this.socialFilterHelper = socialFilterHelper;
        this.redisService = redisService;
        this.authUtils = authUtils;
        this.messagingTemplate = messagingTemplate;
    }

    // *** Push Realtime Notification ***
    private void pushRealtimeNotification(Long recipientId, NotificationDTO dto) {
        if (recipientId == null || dto == null) return;

        try {
            messagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                "/queue/notifications",
                dto
            );
            messagingTemplate.convertAndSend(
                "/topic/notifications." + recipientId,
                dto
            );
        } catch (Exception e) {
            // broker busy; don't let this break the main action
        }

        pushSseNotification(recipientId, dto);
    }

    // *** SSE Subscribe ***
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30 min timeout

        sseEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        // initial ping keeps the browser from closing the connection
        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected to Twine Notification Stream"));
        } catch (IOException e) {
            removeEmitter(userId, emitter);
        }

        return emitter;
    }

    // *** Remove SSE Emitter ***
    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> list = sseEmitters.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) sseEmitters.remove(userId);
        }
    }

    // *** Push SSE Notification ***
    private void pushSseNotification(Long recipientId, NotificationDTO dto) {
        List<SseEmitter> emitters = sseEmitters.get(recipientId);
        if (emitters == null || emitters.isEmpty()) return;

        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("NOTIFICATION").data(dto));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }

    // *** Post Like ***
    @Async("notificationExecutor")
    @Transactional
    public void sendPostLikeNotification(Users actor, PostsEntity post) {
        if (actor == null || post == null || post.getUserpost() == null) return;
        Users recipient = post.getUserpost();

        if (actor.getUserId().equals(recipient.getUserId())) return;
        if (isBlocked(actor, recipient)) return;

        // group multiple likes on the same post into one notification
        Optional<Notification> existingOpt = notificationRepo
            .findFirstByRecipientAndTypeAndTargetIdAndIsReadFalse(
                recipient, NotificationType.POST_LIKE, post.getPostId()
            );

        Notification notification;
        if (existingOpt.isPresent()) {
            Notification existing = existingOpt.get();
            existing.setActor(actor);
            existing.setAggregateCount(existing.getAggregateCount() + 1);
            existing.setUpdatedAt(LocalDateTime.now(ZONE_KOLKATA));
            notification = notificationRepo.save(existing);
        } else {
            notification = new Notification();
            notification.setRecipient(recipient);
            notification.setActor(actor);
            notification.setType(NotificationType.POST_LIKE);
            notification.setTargetId(post.getPostId());
            notification.setPreviewText(post.getFileName());
            notification.setAggregateCount(1);
            notification = notificationRepo.save(notification);
            incrementUnreadCount(recipient);
        }

        pushRealtimeNotification(recipient.getUserId(), toDTO(notification));
    }

    // *** Post Unlike ***
    @Async("notificationExecutor")
    @Transactional
    public void removePostLikeNotification(Users actor, PostsEntity post) {
        if (actor == null || post == null || post.getUserpost() == null) return;
        Users recipient = post.getUserpost();

        Optional<Notification> existingOpt = notificationRepo
            .findFirstByRecipientAndTypeAndTargetIdAndIsReadFalse(
                recipient, NotificationType.POST_LIKE, post.getPostId()
            );

        if (existingOpt.isPresent()) {
            Notification existing = existingOpt.get();
            if (existing.getAggregateCount() > 1) {
                existing.setAggregateCount(existing.getAggregateCount() - 1);
                notificationRepo.save(existing);
            } else {
                notificationRepo.delete(existing);
                decrementUnreadCount(recipient.getUserId());
            }
        }
    }

    // *** Post Comment ***
    @Async("notificationExecutor")
    @Transactional
    public void sendCommentNotification(Users actor, PostsEntity post, String commentText) {
        if (actor == null || post == null || post.getUserpost() == null) return;
        Users recipient = post.getUserpost();

        if (actor.getUserId().equals(recipient.getUserId())) return;
        if (isBlocked(actor, recipient)) return;

        String preview = commentText != null ? commentText.trim() : "";
        if (preview.length() > 80) preview = preview.substring(0, 77) + "...";

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setType(NotificationType.POST_COMMENT);
        notification.setTargetId(post.getPostId());
        notification.setPreviewText(preview);
        notification = notificationRepo.save(notification);

        incrementUnreadCount(recipient);
        pushRealtimeNotification(recipient.getUserId(), toDTO(notification));
    }

    // *** Comment Reply ***
    @Async("notificationExecutor")
    @Transactional
    public void sendReplyNotification(Users actor, PostComment parentComment, String replyText) {
        if (actor == null || parentComment == null || parentComment.getUser() == null) return;
        Users recipient = parentComment.getUser();

        if (actor.getUserId().equals(recipient.getUserId())) return;
        if (isBlocked(actor, recipient)) return;

        String preview = replyText != null ? replyText.trim() : "";
        if (preview.length() > 80) preview = preview.substring(0, 77) + "...";

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setType(NotificationType.COMMENT_REPLY);
        notification.setTargetId(parentComment.getPost() != null ? parentComment.getPost().getPostId() : null);
        notification.setPreviewText(preview);
        notification = notificationRepo.save(notification);

        incrementUnreadCount(recipient);
        pushRealtimeNotification(recipient.getUserId(), toDTO(notification));
    }

    // *** Follow ***
    @Async("notificationExecutor")
    @Transactional
    public void sendFollowNotification(Users actor, Users recipient) {
        if (actor == null || recipient == null) return;
        if (actor.getUserId().equals(recipient.getUserId())) return;
        if (isBlocked(actor, recipient)) return;

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setType(NotificationType.USER_FOLLOW);
        notification = notificationRepo.save(notification);

        incrementUnreadCount(recipient);
        pushRealtimeNotification(recipient.getUserId(), toDTO(notification));
    }

    // *** Follow Request ***
    @Async("notificationExecutor")
    @Transactional
    public void sendFollowRequestNotification(Users actor, Users recipient) {
        if (actor == null || recipient == null) return;
        if (actor.getUserId().equals(recipient.getUserId())) return;
        if (isBlocked(actor, recipient)) return;

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setType(NotificationType.FOLLOW_REQUEST);
        notification = notificationRepo.save(notification);

        incrementUnreadCount(recipient);
        pushRealtimeNotification(recipient.getUserId(), toDTO(notification));
    }

    // *** Cancel Follow Request ***
    @Async("notificationExecutor")
    @Transactional
    public void removeFollowRequestNotification(Users actor, Users recipient) {
        if (actor == null || recipient == null) return;
        notificationRepo.deleteByRecipientAndActorAndType(recipient, actor, NotificationType.FOLLOW_REQUEST);
        decrementUnreadCount(recipient.getUserId());
    }

    // *** Follow Request Accept ***
    @Async("notificationExecutor")
    @Transactional
    public void sendFollowAcceptNotification(Users actor, Users recipient) {
        if (actor == null || recipient == null) return;
        if (actor.getUserId().equals(recipient.getUserId())) return;
        if (isBlocked(actor, recipient)) return;

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setType(NotificationType.FOLLOW_ACCEPT);
        notification = notificationRepo.save(notification);

        incrementUnreadCount(recipient);
        pushRealtimeNotification(recipient.getUserId(), toDTO(notification));
    }

    // *** Secret Crush Match ***
    @Async("notificationExecutor")
    @Transactional
    public void sendSecretCrushMatchNotification(Users userOne, Users userTwo) {
        if (userOne == null || userTwo == null) return;

        Notification notifOne = new Notification();
        notifOne.setRecipient(userOne);
        notifOne.setActor(userTwo);
        notifOne.setType(NotificationType.SECRET_CRUSH_MATCH);
        notificationRepo.save(notifOne);
        incrementUnreadCount(userOne);
        pushRealtimeNotification(userOne.getUserId(), toDTO(notifOne));

        Notification notifTwo = new Notification();
        notifTwo.setRecipient(userTwo);
        notifTwo.setActor(userOne);
        notifTwo.setType(NotificationType.SECRET_CRUSH_MATCH);
        notificationRepo.save(notifTwo);
        incrementUnreadCount(userTwo);
        pushRealtimeNotification(userTwo.getUserId(), toDTO(notifTwo));
    }



    // *** Get Notifications Feed ***
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotifications(int page) {
        Users loggedUser = authUtils.getLoggedUser();
        List<Notification> list = notificationRepo.findFeedByRecipient(
            loggedUser, PageRequest.of(page, 20)
        );

        if (list.isEmpty()) return Collections.emptyList();

        List<NotificationDTO> dtos = new ArrayList<>();
        for (Notification n : list) dtos.add(toDTO(n));
        return dtos;
    }

    // *** Get Unread Count ***
    public long getUnreadCount() {
        Users loggedUser = authUtils.getLoggedUser();
        String key = REDIS_UNREAD_PREFIX + loggedUser.getUserId();
        String cached = redisService.getValue(key);

        if (cached != null) {
            try {
                return Math.max(0, Long.parseLong(cached));
            } catch (NumberFormatException ignored) {}
        }

        // cache miss — hit db and store
        long count = notificationRepo.countUnreadByRecipient(loggedUser);
        redisService.setValueWithExpiry(key, String.valueOf(count), 3600);
        return count;
    }

    // *** Mark As Read ***
    @Transactional
    public void markAsRead(Long notificationId) {
        Users loggedUser = authUtils.getLoggedUser();
        int updated = notificationRepo.markAsRead(notificationId, loggedUser);
        if (updated > 0) decrementUnreadCount(loggedUser.getUserId());
    }

    // *** Mark All As Read ***
    @Transactional
    public void markAllAsRead() {
        Users loggedUser = authUtils.getLoggedUser();
        notificationRepo.markAllAsRead(loggedUser);
        redisService.setValueWithExpiry(REDIS_UNREAD_PREFIX + loggedUser.getUserId(), "0", 3600);
    }

    // *** Is Blocked ***
    private boolean isBlocked(Users actor, Users recipient) {
        Set<Long> blockedIds = socialFilterHelper.getAllBlockedUserIds(actor);
        return blockedIds.contains(recipient.getUserId());
    }

    // *** Increment Unread Count (Users entity passed directly — SecurityContextHolder empty in @Async) ***
    private void incrementUnreadCount(Users recipient) {
        if (recipient == null) return;
        String key = REDIS_UNREAD_PREFIX + recipient.getUserId();
        if (redisService.hasKey(key)) {
            redisService.increment(key);
        } else {
            long count = notificationRepo.countUnreadByRecipient(recipient);
            redisService.setValueWithExpiry(key, String.valueOf(count), 3600);
        }
    }

    // *** Decrement Unread Count ***
    private void decrementUnreadCount(Long userId) {
        String key = REDIS_UNREAD_PREFIX + userId;
        if (redisService.hasKey(key)) {
            Long val = redisService.decrement(key);
            if (val != null && val < 0) redisService.setValue(key, "0");
        }
    }

    // *** To DTO ***
    public NotificationDTO toDTO(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setNotificationId(String.valueOf(n.getNotificationId()));
        dto.setType(n.getType().name());
        dto.setTargetId(n.getTargetId() != null ? String.valueOf(n.getTargetId()) : null);
        dto.setPreviewText(n.getPreviewText());
        dto.setAggregateCount(n.getAggregateCount());
        dto.setRead(n.isRead());
        dto.setCreatedAt(n.getUpdatedAt() != null ? n.getUpdatedAt().toString() : n.getCreatedAt().toString());

        if (n.getActor() != null) {
            dto.setActorId(String.valueOf(n.getActor().getUserId()));
            dto.setActorUsername(n.getActor().getUsername());
            dto.setActorFullname(n.getActor().getFullname());
            dto.setActorVerified(n.getActor().isVerifyTag());
            if (n.getActor().getUserData() != null) {
                dto.setActorProfilePhoto(n.getActor().getUserData().getProfilePhoto());
            }
        }

        dto.setMessage(buildMessage(n));
        return dto;
    }

    // *** Build Message ***
    private String buildMessage(Notification n) {
        String actorName = n.getActor() != null ? n.getActor().getUsername() : "Someone";
        int count = n.getAggregateCount();

        return switch (n.getType()) {
            case POST_LIKE -> {
                if (count <= 1) {
                    yield actorName + " liked your post.";
                } else {
                    yield actorName + " and " + (count - 1) + " others liked your post.";
                }
            }
            case POST_COMMENT -> actorName + " commented on your post: \"" + (n.getPreviewText() != null ? n.getPreviewText() : "") + "\"";
            case COMMENT_REPLY -> actorName + " replied to your comment: \"" + (n.getPreviewText() != null ? n.getPreviewText() : "") + "\"";
            case USER_FOLLOW -> actorName + " started following you.";
            case FOLLOW_REQUEST -> actorName + " sent you a follow request.";
            case FOLLOW_ACCEPT -> actorName + " accepted your follow request.";
            case SECRET_CRUSH_MATCH -> "It's a Match! You and " + actorName + " are Secret Crushes ❤️";
        };
    }
}
