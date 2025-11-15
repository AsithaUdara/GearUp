package com.gearup.exception;

/**
 * Exception thrown when parts are not found
 */
public class PartsNotFoundException extends RuntimeException {

    public PartsNotFoundException(String message) {
        super(message);
    }

    public PartsNotFoundException(Long partId) {
        super("Parts not found with id: " + partId);
    }
}
