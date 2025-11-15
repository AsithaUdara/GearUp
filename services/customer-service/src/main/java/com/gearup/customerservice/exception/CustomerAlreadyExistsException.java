package com.gearup.customerservice.exception;

/**
 * Exception thrown when attempting to create a customer that already exists
 */
public class CustomerAlreadyExistsException extends RuntimeException {

    public CustomerAlreadyExistsException(String message) {
        super(message);
    }

    public CustomerAlreadyExistsException(String firebaseUid) {
        super("Customer already exists with firebase UID: " + firebaseUid);
    }
}
