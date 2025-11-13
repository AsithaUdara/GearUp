package com.gearup.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when validation fails.
 */
public class GearUpValidationException extends BaseException {
    
    public GearUpValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
    }
    
    public GearUpValidationException(String field, String violation) {
        super(String.format("Validation failed for field '%s': %s", field, violation),
              HttpStatus.BAD_REQUEST,
              "VALIDATION_ERROR");
    }
}
