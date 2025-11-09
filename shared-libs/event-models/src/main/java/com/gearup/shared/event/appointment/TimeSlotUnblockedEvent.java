package com.gearup.shared.event.appointment;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Event published when time slots are unblocked
 */
public class TimeSlotUnblockedEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String eventId;
    private LocalDateTime timestamp;
    private String source;
    private Long serviceId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String unblockedBy;
    private LocalDateTime unblockedAt;
    
    public TimeSlotUnblockedEvent() {
        this.timestamp = LocalDateTime.now();
        this.source = "appointment-service";
    }
    
    public TimeSlotUnblockedEvent(String eventId, Long serviceId, LocalDate startDate, LocalDate endDate,
                                  String unblockedBy, LocalDateTime unblockedAt) {
        this();
        this.eventId = eventId;
        this.serviceId = serviceId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.unblockedBy = unblockedBy;
        this.unblockedAt = unblockedAt;
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getUnblockedBy() {
        return unblockedBy;
    }

    public void setUnblockedBy(String unblockedBy) {
        this.unblockedBy = unblockedBy;
    }

    public LocalDateTime getUnblockedAt() {
        return unblockedAt;
    }

    public void setUnblockedAt(LocalDateTime unblockedAt) {
        this.unblockedAt = unblockedAt;
    }
}
