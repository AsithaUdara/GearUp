package com.gearup.notificationservice.dto;

import java.time.LocalDateTime;

import com.gearup.notificationservice.entity.NotificationPriority;
import com.gearup.notificationservice.entity.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationPriority priority;
    private Boolean isRead;
    private String relatedEntityId;
    private String relatedEntityType;
    private String actionUrl;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
