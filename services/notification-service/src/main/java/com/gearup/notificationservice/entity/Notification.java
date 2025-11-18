package com.gearup.notificationservice.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "delivery_channels", columnDefinition = "jsonb")
    private String deliveryChannels; // JSON array: ['EMAIL', 'SMS', 'PUSH', 'IN_APP']
    
    @JdbcTypeCode(SqlTypes.JSON)
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
    
    @Transient
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Transient
    private List<String> deliveryChannelsList;
    
    /**
     * Convert deliveryChannels JSON string to List before persisting
     */
    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (this.deliveryChannelsList != null && !this.deliveryChannelsList.isEmpty()) {
            try {
                this.deliveryChannels = objectMapper.writeValueAsString(this.deliveryChannelsList);
            } catch (JsonProcessingException e) {
                // Fallback to default channels if serialization fails
                this.deliveryChannels = "[\"WEB\", \"IN_APP\"]";
            }
        } else if (this.deliveryChannels == null) {
            // Set default delivery channels if none provided
            this.deliveryChannels = "[\"WEB\", \"IN_APP\"]";
        }
    }
    
    /**
     * Convert deliveryChannels JSON string to List after loading from DB
     */
    @PostLoad
    public void postLoad() {
        if (this.deliveryChannels != null && !this.deliveryChannels.trim().isEmpty()) {
            try {
                this.deliveryChannelsList = objectMapper.readValue(
                    this.deliveryChannels, 
                    new TypeReference<List<String>>() {}
                );
            } catch (JsonProcessingException e) {
                // If parsing fails, set default list
                this.deliveryChannelsList = List.of("WEB", "IN_APP");
            }
        } else {
            this.deliveryChannelsList = List.of("WEB", "IN_APP");
        }
    }
    
    /**
     * Helper method to get delivery channels as List
     */
    public List<String> getDeliveryChannelsAsList() {
        if (this.deliveryChannelsList != null) {
            return this.deliveryChannelsList;
        }
        postLoad(); // Ensure list is populated
        return this.deliveryChannelsList;
    }
    
    /**
     * Helper method to set delivery channels from List
     */
    public void setDeliveryChannelsFromList(List<String> channels) {
        this.deliveryChannelsList = channels;
        if (channels != null && !channels.isEmpty()) {
            try {
                this.deliveryChannels = objectMapper.writeValueAsString(channels);
            } catch (JsonProcessingException e) {
                this.deliveryChannels = "[\"WEB\", \"IN_APP\"]";
            }
        }
    }
}
