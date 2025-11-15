package com.gearup.customerservice.exception;

/**
 * Exception thrown when KYC verification fails
 */
public class KycVerificationException extends RuntimeException {

    public KycVerificationException(String message) {
        super(message);
    }

    public KycVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
