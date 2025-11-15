package com.gearup.analyticalservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for analytical service operations
 */
@Getter
public class AnalyticsException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public AnalyticsException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public AnalyticsException(String message, HttpStatus status) {
        this(message, status, "ANALYTICS_ERROR");
    }
}
