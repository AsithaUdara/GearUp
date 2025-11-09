package com.gearup.shared.event.appointment;

import com.gearup.shared.event.base.BaseAppointmentEvent;

import java.time.LocalDateTime;

/**
 * Event published when an appointment is cancelled
 */
public class AppointmentCancelledEvent extends BaseAppointmentEvent {
    
    private static final long serialVersionUID = 1L;
    
    private Long serviceId;
    private String serviceName;
    private Long timeSlotId;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    
    public AppointmentCancelledEvent() {
        super();
    }
    
    public AppointmentCancelledEvent(String eventId, Long bookingId, String customerId, String customerName,
                                     Long serviceId, String serviceName, Long timeSlotId, 
                                     String cancellationReason, LocalDateTime cancelledAt) {
        super(eventId, bookingId, customerId, customerName);
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.timeSlotId = timeSlotId;
        this.cancellationReason = cancellationReason;
        this.cancelledAt = cancelledAt;
    }

    // Getters and Setters
    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Long getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(Long timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    @Override
    public String toString() {
        return "AppointmentCancelledEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", bookingId=" + getBookingId() +
                ", customerId='" + getCustomerId() + '\'' +
                ", serviceId=" + serviceId +
                ", cancellationReason='" + cancellationReason + '\'' +
                ", cancelledAt=" + cancelledAt +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
