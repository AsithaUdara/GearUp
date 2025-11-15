package com.gearup.appointmentservice.exception;

/**
 * Exception thrown when a service is not found
 */
public class ServiceNotFoundException extends RuntimeException {

    public ServiceNotFoundException(String message) {
        super(message);
    }

    public ServiceNotFoundException(Long serviceId) {
        super("Service not found with id: " + serviceId);
    }
}
