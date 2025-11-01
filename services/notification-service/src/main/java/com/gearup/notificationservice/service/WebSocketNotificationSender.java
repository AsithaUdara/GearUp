package com.gearup.notificationservice.service;

import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.gearup.notificationservice.dto.NotificationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationSender {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send notification to a specific user via WebSocket
     */
    public void sendToUser(String userId, NotificationResponse notification) {
        try {
            String destination = "/queue/notifications";
            messagingTemplate.convertAndSendToUser(userId, destination, notification);
            log.debug("Sent WebSocket notification to user: {}", userId);
        } catch (MessagingException e) {
            log.error("Failed to send WebSocket notification to user: {}", userId, e);
        }
    }

    /**
     * Send unread count update to a specific user
     */
    public void sendUnreadCountUpdate(String userId, long count) {
        try {
            String destination = "/queue/unread-count";
            messagingTemplate.convertAndSendToUser(userId, destination, count);
            log.debug("Sent unread count update to user: {}", userId);
        } catch (MessagingException e) {
            log.error("Failed to send unread count update to user: {}", userId, e);
        }
    }
}
