package com.gearup.analyticalservice.exception;

import com.gearup.shared.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

/**
 * Global exception handler for Analytical Service
 * Extends shared exception handling with service-specific exceptions
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AnalyticsException.class)
    public ResponseEntity<ErrorResponse> handleAnalyticsException(
            AnalyticsException ex, WebRequest request) {
        log.error("Analytics error: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(ex.getStatus().value());
        error.setError(ex.getStatus().getReasonPhrase());
        error.setMessage(ex.getMessage());
        error.setErrorCode(ex.getErrorCode());
        error.setPath(request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(DataAggregationException.class)
    public ResponseEntity<ErrorResponse> handleDataAggregationException(
            DataAggregationException ex, WebRequest request) {
        log.error("Data aggregation error: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setError("Data Aggregation Failed");
        error.setMessage(ex.getMessage());
        error.setErrorCode("DATA_AGGREGATION_ERROR");
        error.setPath(request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error in analytical service: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setError("Internal Server Error");
        error.setMessage("An unexpected error occurred processing analytics");
        error.setErrorCode("ANALYTICS_INTERNAL_ERROR");
        error.setPath(request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
