package com.gearup.shared.event.appointment;

import com.gearup.shared.event.base.BaseAppointmentEvent;

import java.time.LocalDateTime;

/**
 * Event published when appointment details are updated
 */
public class AppointmentUpdatedEvent extends BaseAppointmentEvent {
    
    private static final long serialVersionUID = 1L;
    
    private Long serviceId;
    private String serviceName;
    private String changeDescription;
    private LocalDateTime updatedAt;
    
    public AppointmentUpdatedEvent() {
        super();
    }
    
    public AppointmentUpdatedEvent(String eventId, Long bookingId, String customerId, String customerName,
                                   Long serviceId, String serviceName, String changeDescription,
                                   LocalDateTime updatedAt) {
        super(eventId, bookingId, customerId, customerName);
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.changeDescription = changeDescription;
        this.updatedAt = updatedAt;
    }

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

    public String getChangeDescription() {
        return changeDescription;
    }

    public void setChangeDescription(String changeDescription) {
        this.changeDescription = changeDescription;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "AppointmentUpdatedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", bookingId=" + getBookingId() +
                ", changeDescription='" + changeDescription + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
