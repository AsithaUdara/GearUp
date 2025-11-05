package com.gearup.shared.event;

import java.time.LocalDateTime;

/**
 * Event published when a vehicle booking is created.
 * This event will trigger a notification to the user.
 */
public class VehicleBookingCreatedEvent extends BaseNotificationEvent {
    
    private String bookingId;
    private String vehicleId;
    private String vehicleName;
    private String customerName;
    private LocalDateTime bookingStartDate;
    private LocalDateTime bookingEndDate;
    private Double totalAmount;

    public VehicleBookingCreatedEvent() {
        super();
    }

    public VehicleBookingCreatedEvent(String eventId, String userId, LocalDateTime timestamp,
                                     String bookingId, String vehicleId, String vehicleName,
                                     String customerName, LocalDateTime bookingStartDate,
                                     LocalDateTime bookingEndDate, Double totalAmount) {
        super(eventId, userId, timestamp);
        this.bookingId = bookingId;
        this.vehicleId = vehicleId;
        this.vehicleName = vehicleName;
        this.customerName = customerName;
        this.bookingStartDate = bookingStartDate;
        this.bookingEndDate = bookingEndDate;
        this.totalAmount = totalAmount;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LocalDateTime getBookingStartDate() {
        return bookingStartDate;
    }

    public void setBookingStartDate(LocalDateTime bookingStartDate) {
        this.bookingStartDate = bookingStartDate;
    }

    public LocalDateTime getBookingEndDate() {
        return bookingEndDate;
    }

    public void setBookingEndDate(LocalDateTime bookingEndDate) {
        this.bookingEndDate = bookingEndDate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public String toString() {
        return "VehicleBookingCreatedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", userId='" + getUserId() + '\'' +
                ", timestamp=" + getTimestamp() +
                ", bookingId='" + bookingId + '\'' +
                ", vehicleId='" + vehicleId + '\'' +
                ", vehicleName='" + vehicleName + '\'' +
                ", customerName='" + customerName + '\'' +
                ", bookingStartDate=" + bookingStartDate +
                ", bookingEndDate=" + bookingEndDate +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
