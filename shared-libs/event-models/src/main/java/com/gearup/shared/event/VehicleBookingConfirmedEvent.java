package com.gearup.shared.event;

import java.time.LocalDateTime;

/**
 * Event published when a vehicle booking is confirmed.
 */
public class VehicleBookingConfirmedEvent extends BaseNotificationEvent {
    
    private String bookingId;
    private String vehicleName;
    private String confirmationNumber;
    private LocalDateTime pickupTime;
    private String pickupLocation;

    public VehicleBookingConfirmedEvent() {
        super();
    }

    public VehicleBookingConfirmedEvent(String eventId, String userId, LocalDateTime timestamp,
                                       String bookingId, String vehicleName, String confirmationNumber,
                                       LocalDateTime pickupTime, String pickupLocation) {
        super(eventId, userId, timestamp);
        this.bookingId = bookingId;
        this.vehicleName = vehicleName;
        this.confirmationNumber = confirmationNumber;
        this.pickupTime = pickupTime;
        this.pickupLocation = pickupLocation;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getConfirmationNumber() {
        return confirmationNumber;
    }

    public void setConfirmationNumber(String confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }

    public LocalDateTime getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(LocalDateTime pickupTime) {
        this.pickupTime = pickupTime;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    @Override
    public String toString() {
        return "VehicleBookingConfirmedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", userId='" + getUserId() + '\'' +
                ", timestamp=" + getTimestamp() +
                ", bookingId='" + bookingId + '\'' +
                ", vehicleName='" + vehicleName + '\'' +
                ", confirmationNumber='" + confirmationNumber + '\'' +
                ", pickupTime=" + pickupTime +
                ", pickupLocation='" + pickupLocation + '\'' +
                '}';
    }
}
