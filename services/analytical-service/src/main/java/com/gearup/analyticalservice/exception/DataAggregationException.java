package com.gearup.analyticalservice.exception;

/**
 * Exception thrown when data aggregation fails
 */
public class DataAggregationException extends RuntimeException {

    public DataAggregationException(String message) {
        super(message);
    }

    public DataAggregationException(String message, Throwable cause) {
        super(message, cause);
    }
}
