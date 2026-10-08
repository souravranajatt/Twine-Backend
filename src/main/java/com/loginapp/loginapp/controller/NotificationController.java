package com.loginapp.loginapp.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.loginapp.loginapp.DTO.NotificationDTO;
import com.loginapp.loginapp.Utils.AuthUtils;
import com.loginapp.loginapp.annotation.RateLimit;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.service.NotificationService;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthUtils authUtils;

    public NotificationController(NotificationService notificationService, AuthUtils authUtils) {
        this.notificationService = notificationService;
        this.authUtils = authUtils;
    }

    // paginated feed
    @GetMapping
    @RateLimit(key = "NOTIF_FEED", maxRequests = 30, windowSeconds = 60, message = "Too many requests. Please wait a moment.")
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            @RequestParam(defaultValue = "0") int page) {
        try {
            List<NotificationDTO> feed = notificationService.getNotifications(page);
            return ResponseEntity.ok(feed);
        } catch (IllegalArgumentException e) {
            System.err.println("Notification Feed Bad Request: " + e.getMessage());
            return ResponseEntity.badRequest().body(Collections.emptyList());
        } catch (Exception e) {
            System.err.println("Notification Feed Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    // badge count — reads from Redis cache
    @GetMapping("/unread-count")
    @RateLimit(key = "NOTIF_COUNT", maxRequests = 60, windowSeconds = 60, message = "Too many requests.")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        try {
            long count = notificationService.getUnreadCount();
            return ResponseEntity.ok(Map.of("unreadCount", count));
        } catch (Exception e) {
            System.err.println("Notification Count Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("unreadCount", 0L));
        }
    }

    // Mark Single Notification as Read
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) {
        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok(Map.of("message", "Marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Mark All Notifications as Read
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        try {
            notificationService.markAllAsRead();
            return ResponseEntity.ok(Map.of("message", "All marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // SSE stream for real-time push (browser fallback if WebSocket fails)
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToStream() {
        Users loggedUser = authUtils.getLoggedUser();
        return notificationService.subscribe(loggedUser.getUserId());
    }
}
