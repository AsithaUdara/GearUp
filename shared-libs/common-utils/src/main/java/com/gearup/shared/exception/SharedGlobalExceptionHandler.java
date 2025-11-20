package com.gearup.shared.exception;

import com.gearup.shared.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shared global exception handler that can be imported by all services.
 * Services can extend this or use it directly.
 */
@RestControllerAdvice
public class SharedGlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(SharedGlobalExceptionHandler.class);
    
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, WebRequest request) {
        logger.error("Business exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(ex.getStatus().value());
        error.setError(ex.getStatus().getReasonPhrase());
        error.setMessage(ex.getMessage());
        error.setErrorCode(ex.getErrorCode());
        error.setPath(request.getDescription(false).replace("uri=", ""));
            
        return ResponseEntity.status(ex.getStatus()).body(error);
    }
    
    @ExceptionHandler(GearUpResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            GearUpResourceNotFoundException ex, WebRequest request) {
        logger.error("Resource not found: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setError("Not Found");
        error.setMessage(ex.getMessage());
        error.setErrorCode("RESOURCE_NOT_FOUND");
        error.setPath(request.getDescription(false).replace("uri=", ""));
            
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        logger.error("Validation failed: {}", ex.getMessage());
        
        Map<String, String> validationErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                (existing, replacement) -> existing
            ));
        
        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError("Validation Failed");
        error.setMessage("Input validation failed");
        error.setErrorCode("VALIDATION_ERROR");
        error.setValidationErrors(validationErrors);
        error.setPath(request.getDescription(false).replace("uri=", ""));
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        logger.error("Invalid argument: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError("Bad Request");
        error.setMessage(ex.getMessage());
        error.setErrorCode("INVALID_ARGUMENT");
        error.setPath(request.getDescription(false).replace("uri=", ""));
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setError("Internal Server Error");
        error.setMessage("An unexpected error occurred. Please try again later.");
        error.setErrorCode("INTERNAL_ERROR");
        error.setPath(request.getDescription(false).replace("uri=", ""));
            
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
