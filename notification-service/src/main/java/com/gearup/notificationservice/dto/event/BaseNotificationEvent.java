package com.gearup.notificationservice.dto.event;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseNotificationEvent implements Serializable {
    private String eventId;
    private String userId;
    private LocalDateTime timestamp;
}
