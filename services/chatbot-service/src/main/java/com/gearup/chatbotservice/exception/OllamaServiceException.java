package com.gearup.chatbotservice.exception;

/**
 * Exception thrown when Ollama AI service encounters an error
 */
public class OllamaServiceException extends RuntimeException {

    public OllamaServiceException(String message) {
        super(message);
    }

    public OllamaServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
