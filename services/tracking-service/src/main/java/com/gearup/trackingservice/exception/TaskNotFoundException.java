package com.gearup.trackingservice.exception;

public class TaskNotFoundException extends RuntimeException {
    
    public TaskNotFoundException(String message) {
        super(message);
    }
    
    public TaskNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
