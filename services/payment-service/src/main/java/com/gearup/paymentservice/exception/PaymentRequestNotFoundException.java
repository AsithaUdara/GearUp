package com.gearup.paymentservice.exception;

import java.util.UUID;

public class PaymentRequestNotFoundException extends RuntimeException {
    
    public PaymentRequestNotFoundException(UUID id) {
        super("Payment request not found with ID: " + id);
    }
    
    public PaymentRequestNotFoundException(String message) {
        super(message);
    }
}
