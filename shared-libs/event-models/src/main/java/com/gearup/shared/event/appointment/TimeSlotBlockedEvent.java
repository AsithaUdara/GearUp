package com.gearup.shared.event.appointment;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Event published when time slots are blocked for a date range
 */
public class TimeSlotBlockedEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String eventId;
    private LocalDateTime timestamp;
    private String source;
    private Long serviceId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String blockedBy;
    private LocalDateTime blockedAt;
    
    public TimeSlotBlockedEvent() {
        this.timestamp = LocalDateTime.now();
        this.source = "appointment-service";
    }
    
    public TimeSlotBlockedEvent(String eventId, Long serviceId, LocalDate startDate, LocalDate endDate,
                                String reason, String blockedBy, LocalDateTime blockedAt) {
        this();
        this.eventId = eventId;
        this.serviceId = serviceId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.blockedBy = blockedBy;
        this.blockedAt = blockedAt;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getBlockedBy() {
        return blockedBy;
    }

    public void setBlockedBy(String blockedBy) {
        this.blockedBy = blockedBy;
    }

    public LocalDateTime getBlockedAt() {
        return blockedAt;
    }

    public void setBlockedAt(LocalDateTime blockedAt) {
        this.blockedAt = blockedAt;
    }
}
