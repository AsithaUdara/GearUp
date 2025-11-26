package com.gearup.notificationservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gearup.notificationservice.dto.NotificationRequest;
import com.gearup.notificationservice.dto.NotificationResponse;
import com.gearup.notificationservice.dto.UnreadCountResponse;
import com.gearup.notificationservice.entity.Notification;
import com.gearup.notificationservice.repository.NotificationRepository;
import com.gearup.shared.cache.RedisNotificationCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    // TODO: Integrate actual email delivery service (SendGrid, AWS SES, Mailgun)
    // TODO: Integrate SMS delivery service (Twilio, AWS SNS)
    // TODO: Integrate push notification service (FCM, APNs)
    // TODO: Implement retry mechanism with exponential backoff for failed deliveries
    // TODO: Add dead letter queue for permanently failed notifications
    // TODO: Implement notification templates with variable substitution
    // TODO: Add bulk notification sending with rate limiting
    // TODO: Implement notification scheduling for future delivery
    // TODO: Add notification preferences per user (email, SMS, push toggles)
    // TODO: Implement notification batching for digest emails
    // TODO: Add delivery status tracking (sent, delivered, failed, read)
    // TODO: Implement notification priority queue (critical, high, normal, low)
    // TODO: Add notification analytics (open rates, click-through rates)
    // TODO: Implement notification archival and cleanup policies
    // TODO: Add A/B testing capability for notification content
    // TODO: Implement notification delivery receipts and read confirmations
    // TODO: Add multi-language support for notification content

    private final NotificationRepository notificationRepository;
    private final RedisNotificationCache redisNotificationCache;
    private final WebSocketNotificationSender webSocketNotificationSender;

    /**
     * Create and persist a new notification
     */
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        log.debug("Creating notification for user: {}", request.getUserId());
        
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .priority(request.getPriority())
                .relatedEntityId(request.getRelatedEntityId())
                .relatedEntityType(request.getRelatedEntityType())
                .actionUrl(request.getActionUrl())
                .deliveryChannels(request.getDeliveryChannels())
                .metadata(request.getMetadata())
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        
        // Update Redis cache
        redisNotificationCache.incrementUnreadCount(request.getUserId());
        
        // Send real-time notification via WebSocket
        NotificationResponse response = mapToResponse(savedNotification);
        webSocketNotificationSender.sendToUser(request.getUserId(), response);
        
        log.info("Notification created with ID: {} for user: {}", savedNotification.getId(), request.getUserId());
        
        return response;
    }

    /**
     * Get paginated notifications for a user
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(String userId, int page, int size) {
        log.debug("Fetching notifications for user: {}, page: {}, size: {}", userId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        return notifications.map(this::mapToResponse);
    }

    /**
     * Get unread notifications for a user
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(String userId) {
        log.debug("Fetching unread notifications for user: {}", userId);
        
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        
        return notifications.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get unread notification count
     */
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(String userId) {
        log.debug("Fetching unread count for user: {}", userId);
        
        // Try to get from Redis cache first
        Long cachedCount = redisNotificationCache.getUnreadCount(userId);
        if (cachedCount != null) {
            return new UnreadCountResponse(cachedCount);
        }
        
        // If not in cache, get from database and update cache
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        redisNotificationCache.setUnreadCount(userId, count);
        
        return new UnreadCountResponse(count);
    }

    /**
     * Mark a single notification as read
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, String userId) {
        log.debug("Marking notification {} as read for user: {}", notificationId, userId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + notificationId));

        // Verify ownership
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to notification");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
            
            // Update Redis cache
            redisNotificationCache.decrementUnreadCount(userId);
        }

        return mapToResponse(notification);
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public int markAllAsRead(String userId) {
        log.debug("Marking all notifications as read for user: {}", userId);
        
        int updatedCount = notificationRepository.markAllAsReadByUserId(userId);
        
        // Reset Redis cache
        redisNotificationCache.setUnreadCount(userId, 0L);
        
        log.info("Marked {} notifications as read for user: {}", updatedCount, userId);
        
        return updatedCount;
    }

    /**
     * Delete a notification
     */
    @Transactional
    public void deleteNotification(Long notificationId, String userId) {
        log.debug("Deleting notification {} for user: {}", notificationId, userId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + notificationId));

        // Verify ownership
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to notification");
        }

        if (!notification.getIsRead()) {
            redisNotificationCache.decrementUnreadCount(userId);
        }

        notificationRepository.delete(notification);
        
        log.info("Deleted notification with ID: {} for user: {}", notificationId, userId);
    }

    /**
     * Map entity to response DTO
     */
    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .priority(notification.getPriority())
                .isRead(notification.getIsRead())
                .relatedEntityId(notification.getRelatedEntityId())
                .relatedEntityType(notification.getRelatedEntityType())
                .actionUrl(notification.getActionUrl())
                .deliveryChannels(notification.getDeliveryChannels())
                .metadata(notification.getMetadata())
                .sentAt(notification.getSentAt())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
