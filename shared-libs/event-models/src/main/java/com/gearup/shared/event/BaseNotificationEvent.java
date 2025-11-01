package com.gearup.shared.event;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base DTO for notification-related events.
 * Simple POJO (no Lombok) to avoid annotation processor/tooling incompatibilities in build environments.
 */
public abstract class BaseNotificationEvent implements Serializable {
    private String eventId;
    private String userId;
    private LocalDateTime timestamp;

    public BaseNotificationEvent() {
    }

    public BaseNotificationEvent(String eventId, String userId, LocalDateTime timestamp) {
        this.eventId = eventId;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
