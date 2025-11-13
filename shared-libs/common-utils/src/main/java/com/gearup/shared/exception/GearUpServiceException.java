package com.gearup.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a service operation fails.
 */
public class GearUpServiceException extends BaseException {
    
    public GearUpServiceException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "SERVICE_ERROR");
    }
    
    public GearUpServiceException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR, "SERVICE_ERROR");
    }
}
