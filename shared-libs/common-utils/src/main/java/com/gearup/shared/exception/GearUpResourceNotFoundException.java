package com.gearup.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 * Using GearUpResourceNotFoundException to avoid conflicts with existing service exceptions.
 */
public class GearUpResourceNotFoundException extends BaseException {
    
    public GearUpResourceNotFoundException(String resource, String identifier) {
        super(String.format("%s not found with identifier: %s", resource, identifier), 
              HttpStatus.NOT_FOUND, 
              "RESOURCE_NOT_FOUND");
    }
    
    public GearUpResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}
