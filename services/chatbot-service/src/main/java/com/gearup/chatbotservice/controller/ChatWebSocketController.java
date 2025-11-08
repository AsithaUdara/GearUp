package com.gearup.chatbotservice.controller;

import com.gearup.chatbotservice.dto.ChatMessageRequest;
import com.gearup.chatbotservice.dto.ChatMessageResponse;
import com.gearup.chatbotservice.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.security.Principal;

/**
 * WebSocket Controller for real-time chat
 * Handles incoming messages from clients via WebSocket
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketController {
    
    private final ChatbotService chatbotService;
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Handle incoming chat messages via WebSocket
     * Endpoint: /app/chat.send
     * 
     * @param request chat message from client
     * @param principal authenticated user
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Validated @Payload ChatMessageRequest request, 
                           Principal principal) {
        
        String userId = principal != null ? principal.getName() : "anonymous";
        request.setUserId(userId);
        
        log.info("Received WebSocket message from user {}: {}", 
            userId, 
            request.getContent().length() > 50 
                ? request.getContent().substring(0, 50) + "..." 
                : request.getContent());
        
        try {
            // Process message asynchronously
            ChatMessageResponse response = chatbotService.processMessage(request);
            
            // Send response back to user
            messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/messages",
                response
            );
            
            log.debug("Sent response to user {}", userId);
            
        } catch (Exception e) {
            log.error("Error processing WebSocket message from user {}", userId, e);
            
            // Send error response
            ChatMessageResponse errorResponse = ChatMessageResponse.builder()
                .content("Sorry, I encountered an error processing your message. Please try again.")
                .sessionId(request.getSessionId())
                .timestamp(System.currentTimeMillis())
                .sender("bot")
                .build();
            
            messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/messages",
                errorResponse
            );
        }
    }
    
    /**
     * Handle typing indicator
     * Endpoint: /app/chat.typing
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload String sessionId, Principal principal) {
        String userId = principal != null ? principal.getName() : "anonymous";
        
        // Broadcast typing indicator to all users in session (if needed for group chat)
        // For 1-on-1 with bot, this is optional
        log.debug("User {} is typing in session {}", userId, sessionId);
    }
}
