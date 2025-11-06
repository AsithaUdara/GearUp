package com.gearup.chatbotservice.service;

import com.gearup.chatbotservice.dto.ChatMessageRequest;
import com.gearup.chatbotservice.dto.ChatMessageResponse;
import com.gearup.chatbotservice.entity.ConversationHistory;
import com.gearup.chatbotservice.entity.ConversationHistory.MessageSender;
import com.gearup.chatbotservice.repository.ConversationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Main Chatbot Service
 * Orchestrates the chatbot workflow: intent classification, RAG, action handling
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatbotService {
    
    private final RAGService ragService;
    private final IntentClassifierService intentClassifier;
    private final ActionHandlerService actionHandler;
    private final ConversationHistoryRepository historyRepository;
    private final ChatSessionService sessionService;
    
    /**
     * Process incoming chat message
     * 
     * @param request chat message request
     * @return chat response
     */
    @Transactional
    public ChatMessageResponse processMessage(ChatMessageRequest request) {
        
        String message = request.getContent();
        String userId = request.getUserId();
        String sessionId = request.getSessionId();
        
        log.info("Processing message from user {} in session {}", userId, sessionId);
        
        try {
            // Ensure session exists and is active
            sessionService.ensureSessionActive(sessionId, userId);
            
            // Save user message
            saveMessage(sessionId, userId, message, MessageSender.USER, null);
            
            // Classify intent
            String intent = intentClassifier.classifyIntent(message);
            log.debug("Classified intent: {}", intent);
            
            // Generate response based on intent
            String responseContent = generateResponse(intent, message, userId);
            
            // Save bot response
            saveMessage(sessionId, userId, responseContent, MessageSender.BOT, intent);
            
            // Build response DTO
            ChatMessageResponse response = ChatMessageResponse.builder()
                .content(responseContent)
                .sessionId(sessionId)
                .intent(intent)
                .timestamp(System.currentTimeMillis())
                .sender("bot")
                .createdAt(LocalDateTime.now())
                .build();
            
            // Add suggested actions based on intent
            response.setSuggestedActions(getSuggestedActions(intent));
            
            log.info("Successfully processed message for user {}", userId);
            return response;
            
        } catch (Exception e) {
            log.error("Error processing message", e);
            return buildErrorResponse(sessionId);
        }
    }
    
    /**
     * Generate response based on intent
     */
    private String generateResponse(String intent, String message, String userId) {
        
        // Action-based intents (booking, status, modify)
        if (isActionIntent(intent)) {
            return actionHandler.handleAction(intent, message, userId);
        }
        
        // Information intents - use RAG
        return ragService.processQuery(message, userId);
    }
    
    /**
     * Check if intent requires action handling
     */
    private boolean isActionIntent(String intent) {
        return List.of("booking", "status", "modify").contains(intent);
    }
    
    /**
     * Save message to conversation history
     */
    private void saveMessage(String sessionId, String userId, String message, 
                            MessageSender sender, String intent) {
        
        ConversationHistory history = ConversationHistory.builder()
            .sessionId(sessionId)
            .userId(userId)
            .message(message)
            .sender(sender)
            .intent(intent)
            .build();
        
        historyRepository.save(history);
    }
    
    /**
     * Get suggested actions based on intent
     */
    private List<String> getSuggestedActions(String intent) {
        return switch (intent) {
            case "booking" -> List.of("View Services", "Check Availability", "Contact Support");
            case "status" -> List.of("Track Booking", "Call Support", "View History");
            case "pricing" -> List.of("View All Services", "Book Service", "Get Quote");
            case "services" -> List.of("View Services", "Compare Packages", "Book Now");
            default -> List.of("Book Service", "View Services", "Contact Support");
        };
    }
    
    /**
     * Build error response
     */
    private ChatMessageResponse buildErrorResponse(String sessionId) {
        return ChatMessageResponse.builder()
            .content("I apologize, but I encountered an error. Please try again or contact support.")
            .sessionId(sessionId)
            .timestamp(System.currentTimeMillis())
            .sender("bot")
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    /**
     * Get conversation history for a session
     * 
     * @param sessionId session identifier
     * @return list of conversation history
     */
    public List<ConversationHistory> getConversationHistory(String sessionId) {
        return historyRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }
    
    /**
     * Get recent messages in a session
     * 
     * @param sessionId session identifier
     * @param limit maximum number of messages
     * @return list of recent messages
     */
    public List<ConversationHistory> getRecentMessages(String sessionId, int limit) {
        return historyRepository.findRecentMessagesBySession(sessionId, limit);
    }
}
