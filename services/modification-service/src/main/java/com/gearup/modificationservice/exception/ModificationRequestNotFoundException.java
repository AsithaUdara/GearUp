package com.gearup.modificationservice.exception;

/**
 * Exception thrown when a modification request is not found
 */
public class ModificationRequestNotFoundException extends RuntimeException {

    public ModificationRequestNotFoundException(String message) {
        super(message);
    }

    public ModificationRequestNotFoundException(Long requestId) {
        super("Modification request not found with id: " + requestId);
    }
}
