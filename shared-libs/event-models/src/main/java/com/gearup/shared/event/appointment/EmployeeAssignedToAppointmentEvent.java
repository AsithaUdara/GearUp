package com.gearup.shared.event.appointment;

import com.gearup.shared.event.base.BaseAppointmentEvent;

import java.time.LocalDateTime;

/**
 * CRITICAL EVENT: Published when an employee is assigned to an appointment
 * This triggers task creation in tracking service and notifications
 */
public class EmployeeAssignedToAppointmentEvent extends BaseAppointmentEvent {
    
    private static final long serialVersionUID = 1L;
    
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private Long serviceId;
    private String serviceName;
    private LocalDateTime appointmentDateTime;
    private LocalDateTime assignedAt;
    private String timeSlot;
    
    public EmployeeAssignedToAppointmentEvent() {
        super();
    }
    
    public EmployeeAssignedToAppointmentEvent(String eventId, Long bookingId, String customerId, String customerName,
                                              Long employeeId, String employeeName, String employeeEmail,
                                              Long serviceId, String serviceName, 
                                              LocalDateTime appointmentDateTime, LocalDateTime assignedAt, String timeSlot) {
        super(eventId, bookingId, customerId, customerName);
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.appointmentDateTime = appointmentDateTime;
        this.assignedAt = assignedAt;
        this.timeSlot = timeSlot;
    }

    // Getters and Setters
    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
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

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    @Override
    public String toString() {
        return "EmployeeAssignedToAppointmentEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", bookingId=" + getBookingId() +
                ", customerId='" + getCustomerId() + '\'' +
                ", customerName='" + getCustomerName() + '\'' +
                ", employeeId=" + employeeId +
                ", employeeName='" + employeeName + '\'' +
                ", serviceId=" + serviceId +
                ", serviceName='" + serviceName + '\'' +
                ", appointmentDateTime=" + appointmentDateTime +
                ", assignedAt=" + assignedAt +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
