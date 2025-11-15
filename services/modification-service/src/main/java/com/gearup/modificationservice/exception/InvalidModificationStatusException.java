package com.gearup.modificationservice.exception;

/**
 * Exception thrown when an invalid modification status transition is attempted
 */
public class InvalidModificationStatusException extends RuntimeException {

    public InvalidModificationStatusException(String message) {
        super(message);
    }

    public InvalidModificationStatusException(String fromStatus, String toStatus) {
        super(String.format("Invalid status transition from %s to %s", fromStatus, toStatus));
    }
}
