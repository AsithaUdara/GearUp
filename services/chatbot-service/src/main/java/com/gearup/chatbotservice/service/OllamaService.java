package com.gearup.chatbotservice.service;

import com.gearup.chatbotservice.config.OllamaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Service for interacting with Ollama LLM
 * Handles chat completions and response generation
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OllamaService {
    
    @Qualifier("ollamaWebClient")
    private final WebClient ollamaWebClient;
    
    private final OllamaProperties ollamaProperties;
    
    private static final String SYSTEM_PROMPT = """
        You are a helpful assistant for GearUp Auto Service, a vehicle maintenance and repair service platform.
        
        Your capabilities include:
        - Answering questions about services, pricing, and scheduling
        - Helping users book appointments
        - Checking booking status
        - Providing information about vehicle maintenance
        
        Guidelines:
        - Be concise and friendly
        - Answer based on the provided context
        - If you don't know something, politely say so
        - For booking requests, gather: service type, date, time, vehicle details
        - Always be professional and helpful
        """;
    
    /**
     * Generate response using Ollama LLM
     * 
     * @param userQuery user's question or message
     * @param context relevant context from knowledge base (optional)
     * @return generated response
     */
    public String generateResponse(String userQuery, String context) {
        
        String userPrompt = buildUserPrompt(userQuery, context);
        
        try {
            log.debug("Generating response for query: {}", 
                userQuery.length() > 100 ? userQuery.substring(0, 100) + "..." : userQuery);
            
            OllamaResponse response = ollamaWebClient.post()
                .uri("/api/chat")
                .bodyValue(Map.of(
                    "model", ollamaProperties.getChatModel(),
                    "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", userPrompt)
                    ),
                    "stream", false,
                    "options", Map.of(
                        "temperature", 0.7,
                        "top_p", 0.9,
                        "top_k", 40
                    )
                ))
                .retrieve()
                .bodyToMono(OllamaResponse.class)
                .retryWhen(Retry.backoff(ollamaProperties.getMaxRetries(), Duration.ofSeconds(2))
                    .doBeforeRetry(signal -> 
                        log.warn("Retrying Ollama request, attempt: {}", signal.totalRetries() + 1)))
                .block();
            
            if (response == null || response.getMessage() == null) {
                throw new RuntimeException("Null response from Ollama");
            }
            
            String generatedResponse = response.getMessage().getContent();
            log.debug("Successfully generated response with length: {}", generatedResponse.length());
            
            return generatedResponse;
            
        } catch (Exception e) {
            log.error("Failed to generate response from Ollama", e);
            return getFallbackResponse();
        }
    }
    
    /**
     * Generate response with conversation history
     * 
     * @param userQuery current user query
     * @param context relevant context
     * @param conversationHistory previous messages in conversation
     * @return generated response
     */
    public String generateResponseWithHistory(String userQuery, String context, 
                                              List<Map<String, String>> conversationHistory) {
        try {
            String userPrompt = buildUserPrompt(userQuery, context);
            
            // Build messages list with history
            var messages = new java.util.ArrayList<Map<String, String>>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            
            // Add conversation history (last N messages)
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                int historyLimit = Math.min(conversationHistory.size(), 5);
                messages.addAll(conversationHistory.subList(
                    Math.max(0, conversationHistory.size() - historyLimit), 
                    conversationHistory.size()
                ));
            }
            
            // Add current query
            messages.add(Map.of("role", "user", "content", userPrompt));
            
            OllamaResponse response = ollamaWebClient.post()
                .uri("/api/chat")
                .bodyValue(Map.of(
                    "model", ollamaProperties.getChatModel(),
                    "messages", messages,
                    "stream", false,
                    "options", Map.of(
                        "temperature", 0.7,
                        "top_p", 0.9
                    )
                ))
                .retrieve()
                .bodyToMono(OllamaResponse.class)
                .block();
            
            return response != null && response.getMessage() != null 
                ? response.getMessage().getContent()
                : getFallbackResponse();
                
        } catch (Exception e) {
            log.error("Failed to generate response with history", e);
            return getFallbackResponse();
        }
    }
    
    /**
     * Build user prompt with context
     */
    private String buildUserPrompt(String userQuery, String context) {
        if (context == null || context.isBlank()) {
            return userQuery;
        }
        
        return String.format("""
            Context from knowledge base:
            %s
            
            User Question: %s
            
            Please answer the question based on the context above. If the context doesn't contain the answer, 
            you can provide general information but mention that specific details aren't available.
            """, context, userQuery);
    }
    
    /**
     * Get fallback response when LLM fails
     */
    private String getFallbackResponse() {
        return "I apologize, but I'm having trouble processing your request right now. " +
               "Please try again in a moment, or contact our support team for immediate assistance.";
    }
    
    /**
     * Check if Ollama service is available
     * 
     * @return true if service is healthy
     */
    public boolean isServiceAvailable() {
        try {
            ollamaWebClient.get()
                .uri("/api/tags")
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(5));
            return true;
        } catch (Exception e) {
            log.warn("Ollama service is not available: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Response DTO for Ollama chat API
     */
    @lombok.Data
    private static class OllamaResponse {
        private Message message;
        private String model;
        private String created_at;
        private boolean done;
    }
    
    @lombok.Data
    private static class Message {
        private String role;
        private String content;
    }
}
