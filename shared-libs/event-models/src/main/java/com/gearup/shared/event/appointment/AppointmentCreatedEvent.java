package com.gearup.shared.event.appointment;

import com.gearup.shared.event.base.BaseAppointmentEvent;

import java.time.LocalDateTime;

/**
 * Event published when a new appointment/booking is created
 */
public class AppointmentCreatedEvent extends BaseAppointmentEvent {
    
    private static final long serialVersionUID = 1L;
    
    private Long serviceId;
    private String serviceName;
    private Long timeSlotId;
    private LocalDateTime slotDateTime;
    private String customerEmail;
    private String customerPhone;
    private String status;
    private String notes;
    
    public AppointmentCreatedEvent() {
        super();
    }
    
    public AppointmentCreatedEvent(String eventId, Long bookingId, String customerId, String customerName,
                                   Long serviceId, String serviceName, Long timeSlotId, LocalDateTime slotDateTime,
                                   String customerEmail, String customerPhone, String status, String notes) {
        super(eventId, bookingId, customerId, customerName);
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.timeSlotId = timeSlotId;
        this.slotDateTime = slotDateTime;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.status = status;
        this.notes = notes;
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

    public LocalDateTime getSlotDateTime() {
        return slotDateTime;
    }

    public void setSlotDateTime(LocalDateTime slotDateTime) {
        this.slotDateTime = slotDateTime;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "AppointmentCreatedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", bookingId=" + getBookingId() +
                ", customerId='" + getCustomerId() + '\'' +
                ", customerName='" + getCustomerName() + '\'' +
                ", serviceId=" + serviceId +
                ", serviceName='" + serviceName + '\'' +
                ", timeSlotId=" + timeSlotId +
                ", slotDateTime=" + slotDateTime +
                ", status='" + status + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
