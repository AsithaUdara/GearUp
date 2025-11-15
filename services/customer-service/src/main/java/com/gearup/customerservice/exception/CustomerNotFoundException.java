package com.gearup.customerservice.exception;

/**
 * Exception thrown when a customer is not found
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String message) {
        super(message);
    }

    public CustomerNotFoundException(String firebaseUid) {
        super("Customer not found with firebase UID: " + firebaseUid);
    }
}
