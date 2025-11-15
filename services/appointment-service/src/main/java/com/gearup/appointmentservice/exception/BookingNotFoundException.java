package com.gearup.appointmentservice.exception;

/**
 * Exception thrown when a booking is not found
 */
public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(String message) {
        super(message);
    }

    public BookingNotFoundException(Long bookingId) {
        super("Booking not found with id: " + bookingId);
    }
}
