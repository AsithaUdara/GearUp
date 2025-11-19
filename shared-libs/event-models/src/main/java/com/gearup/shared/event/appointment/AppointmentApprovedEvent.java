package com.gearup.shared.event.appointment;

import com.gearup.shared.event.base.BaseAppointmentEvent;

import java.time.LocalDateTime;

/**
 * Event published when an appointment is approved by admin
 */
public class AppointmentApprovedEvent extends BaseAppointmentEvent {
    
    private static final long serialVersionUID = 1L;
    
    private Long serviceId;
    private String serviceName;
    private String approvedBy;
    private LocalDateTime approvedAt;
    
    public AppointmentApprovedEvent() {
        super();
    }
    
    public AppointmentApprovedEvent(String eventId, Long bookingId, String customerId, String customerName,
                                    Long serviceId, String serviceName, String approvedBy, LocalDateTime approvedAt) {
        super(eventId, bookingId, customerId, customerName);
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
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

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
}
