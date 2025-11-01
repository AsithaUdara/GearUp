package com.gearup.notificationservice.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gearup.notificationservice.dto.NotificationRequest;
import com.gearup.notificationservice.dto.NotificationResponse;
import com.gearup.notificationservice.dto.UnreadCountResponse;
import com.gearup.notificationservice.service.NotificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Create a new notification (typically called by other microservices)
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        log.info("Received request to create notification for user: {}", request.getUserId());
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get paginated notifications for the authenticated user
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        String userId = authentication.getName();
        log.info("Fetching notifications for user: {}, page: {}, size: {}", userId, page, size);
        
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(userId, page, size);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notifications for the authenticated user
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(Authentication authentication) {
        String userId = authentication.getName();
        log.info("Fetching unread notifications for user: {}", userId);
        
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notification count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(Authentication authentication) {
        String userId = authentication.getName();
        log.debug("Fetching unread count for user: {}", userId);
        
        UnreadCountResponse count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Mark a notification as read
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {
        
        String userId = authentication.getName();
        log.info("Marking notification {} as read for user: {}", notificationId, userId);
        
        NotificationResponse response = notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark all notifications as read
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        String userId = authentication.getName();
        log.info("Marking all notifications as read for user: {}", userId);
        
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a notification
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId,
            Authentication authentication) {
        
        String userId = authentication.getName();
        log.info("Deleting notification {} for user: {}", notificationId, userId);
        
        notificationService.deleteNotification(notificationId, userId);
        return ResponseEntity.noContent().build();
    }
}
