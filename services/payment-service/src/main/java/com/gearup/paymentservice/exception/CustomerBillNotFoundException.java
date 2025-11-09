package com.gearup.paymentservice.exception;

import java.util.UUID;

public class CustomerBillNotFoundException extends RuntimeException {
    
    public CustomerBillNotFoundException(UUID id) {
        super("Customer bill not found with ID: " + id);
    }
    
    public CustomerBillNotFoundException(String message) {
        super(message);
    }
}
