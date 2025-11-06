package com.gearup.notificationservice.dto;

import com.gearup.notificationservice.entity.NotificationPriority;
import com.gearup.notificationservice.entity.NotificationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Message is required")
    private String message;
    
    @NotNull(message = "Type is required")
    private NotificationType type;
    
    @NotNull(message = "Priority is required")
    private NotificationPriority priority;
    
    private String relatedEntityId;
    private String relatedEntityType;
    private String actionUrl;
    
    // New fields for enhanced notification support
    private String deliveryChannels; // JSON array: ["EMAIL", "SMS", "PUSH", "IN_APP"]
    private String metadata; // Additional metadata in JSON format
}
