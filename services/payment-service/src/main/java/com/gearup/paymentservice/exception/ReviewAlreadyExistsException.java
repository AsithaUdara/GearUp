package com.gearup.paymentservice.exception;

import java.util.UUID;

public class ReviewAlreadyExistsException extends RuntimeException {
    public ReviewAlreadyExistsException(UUID billId) {
        super("Review already exists for bill: " + billId);
    }
}
