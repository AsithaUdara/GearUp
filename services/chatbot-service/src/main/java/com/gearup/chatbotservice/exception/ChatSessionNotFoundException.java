package com.gearup.chatbotservice.exception;

/**
 * Exception thrown when a chat session is not found
 */
public class ChatSessionNotFoundException extends RuntimeException {

    public ChatSessionNotFoundException(String message) {
        super(message);
    }

    public ChatSessionNotFoundException(String sessionId, String userId) {
        super(String.format("Chat session not found with id: %s for user: %s", sessionId, userId));
    }
}
