package com.gearup.vehicleservice.exception;

/**
 * Exception thrown when attempting to create a vehicle that already exists
 */
public class VehicleAlreadyExistsException extends RuntimeException {

    public VehicleAlreadyExistsException(String message) {
        super(message);
    }

    public VehicleAlreadyExistsException(String vehicleId) {
        super("Vehicle already exists with id: " + vehicleId);
    }
}
