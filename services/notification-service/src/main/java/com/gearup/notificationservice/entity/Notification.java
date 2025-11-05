package com.gearup.notificationservice.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notification", indexes = {
    @Index(name = "idx_notification_user_id", columnList = "user_id"),
    @Index(name = "idx_notification_user_is_read", columnList = "user_id, is_read"),
    @Index(name = "idx_notification_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;
    
    @Column(name = "user_id", nullable = false, length = 128)
    private String userId; // Firebase UID
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationPriority priority;
    
    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "related_entity_id", length = 100)
    private String relatedEntityId; // e.g., invoice ID, task ID
    
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType; // e.g., "INVOICE", "TASK"
    
    @Column(name = "action_url", length = 500)
    private String actionUrl; // Deep link for frontend navigation
    
    @Column(name = "delivery_channels", columnDefinition = "jsonb")
    private String deliveryChannels; // JSON array: ['EMAIL', 'SMS', 'PUSH', 'IN_APP']
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata; // Additional metadata in JSON format
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt; // When notification was sent/delivered
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt; // When user marked as read
}
