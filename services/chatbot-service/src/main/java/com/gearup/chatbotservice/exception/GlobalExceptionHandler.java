package com.gearup.chatbotservice.exception;

import com.gearup.shared.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler for Chatbot Service
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ChatSessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleChatSessionNotFoundException(
            ChatSessionNotFoundException ex, WebRequest request) {
        log.error("Chat session not found: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setError("Not Found");
        error.setMessage(ex.getMessage());
        error.setErrorCode("CHAT_SESSION_NOT_FOUND");
        error.setPath(request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(OllamaServiceException.class)
    public ResponseEntity<ErrorResponse> handleOllamaServiceException(
            OllamaServiceException ex, WebRequest request) {
        log.error("Ollama service error: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        error.setError("Service Unavailable");
        error.setMessage("AI service is temporarily unavailable. Please try again later.");
        error.setErrorCode("AI_SERVICE_ERROR");
        error.setPath(request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation failed: {}", ex.getMessage());

        var validationErrors = ex.getBindingResult()
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error in chatbot service: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setError("Internal Server Error");
        error.setMessage("An unexpected error occurred processing your chat request");
        error.setErrorCode("CHATBOT_INTERNAL_ERROR");
        error.setPath(request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
