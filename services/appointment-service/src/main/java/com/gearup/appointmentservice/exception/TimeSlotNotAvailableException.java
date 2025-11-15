package com.gearup.appointmentservice.exception;

/**
 * Exception thrown when a time slot is not available for booking
 */
public class TimeSlotNotAvailableException extends RuntimeException {

    public TimeSlotNotAvailableException(String message) {
        super(message);
    }

    public TimeSlotNotAvailableException(Long timeSlotId) {
        super("Time slot is not available for booking: " + timeSlotId);
    }
}
