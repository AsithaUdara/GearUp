package com.gearup.chatbotservice.controller;

import com.gearup.chatbotservice.dto.ChatMessageRequest;
import com.gearup.chatbotservice.dto.ChatMessageResponse;
import com.gearup.chatbotservice.entity.ChatSession;
import com.gearup.chatbotservice.entity.ConversationHistory;
import com.gearup.chatbotservice.service.ChatbotService;
import com.gearup.chatbotservice.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for chat operations
 * Provides HTTP endpoints for chat functionality
 */
@RestController
@RequestMapping("/api/chat")
@Slf4j
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatbotService chatbotService;
    private final ChatSessionService sessionService;
    
    /**
     * Send a message (REST alternative to WebSocket)
     * POST /api/chat/send
     */
    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @Validated @RequestBody ChatMessageRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId != null) {
            request.setUserId(userId);
        }
        
        log.info("Received REST message from user {}", userId);
        
        ChatMessageResponse response = chatbotService.processMessage(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get conversation history for a session
     * GET /api/chat/history/{sessionId}
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ConversationHistory>> getHistory(
            @PathVariable("sessionId") String sessionId) {
        
        List<ConversationHistory> history = chatbotService.getConversationHistory(sessionId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Get recent messages in a session
     * GET /api/chat/history/{sessionId}/recent
     */
    @GetMapping("/history/{sessionId}/recent")
    public ResponseEntity<List<ConversationHistory>> getRecentHistory(
            @PathVariable("sessionId") String sessionId,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<ConversationHistory> history = chatbotService.getRecentMessages(sessionId, limit);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Get all sessions for a user
     * GET /api/chat/sessions
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSession>> getUserSessions(
            @RequestHeader("X-User-Id") String userId) {
        
        List<ChatSession> sessions = sessionService.getUserSessions(userId);
        return ResponseEntity.ok(sessions);
    }
    
    /**
     * Close a session
     * POST /api/chat/sessions/{sessionId}/close
     */
    @PostMapping("/sessions/{sessionId}/close")
    public ResponseEntity<Void> closeSession(@PathVariable("sessionId") String sessionId) {
        sessionService.closeSession(sessionId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Health check endpoint
     * GET /api/chat/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chatbot service is running");
    }
}
