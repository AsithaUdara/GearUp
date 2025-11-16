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
    
    /**
     * Helper method to get default delivery channels as JSON string
     */
    public static String getDefaultDeliveryChannels() {
        return "[\"WEB\", \"IN_APP\"]";
    }
    
    /**
     * Helper method to validate and ensure delivery channels are properly formatted
     */
    public String getValidatedDeliveryChannels() {
        if (this.deliveryChannels == null || this.deliveryChannels.trim().isEmpty()) {
            return getDefaultDeliveryChannels();
        }
        
        // Basic validation to ensure it's a JSON array
        String trimmed = this.deliveryChannels.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            return trimmed;
        } else {
            // If it's not a JSON array, wrap it as one
            return "[\"" + trimmed + "\"]";
        }
    }
}
