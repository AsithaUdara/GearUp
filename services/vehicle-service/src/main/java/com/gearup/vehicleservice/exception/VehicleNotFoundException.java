package com.gearup.vehicleservice.exception;

/**
 * Exception thrown when a vehicle is not found
 */
public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(String message) {
        super(message);
    }

    public VehicleNotFoundException(String vehicleId) {
        super("Vehicle not found with id: " + vehicleId);
    }
}
