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

    private final ResponseProcessorService responseProcessor;
    
    private static final String SYSTEM_PROMPT = """
        You are GearUp's AI assistant for vehicle maintenance bookings.

        CRITICAL RULES - Follow these strictly:
        1. Keep ALL responses under 3 sentences
        2. Be direct - answer first, explain only if essential
        3. Use simple, everyday language - no technical jargon
        4. Never repeat information the user already provided
        5. For booking: ask ONLY for missing details (service, date, time, vehicle)

        Response Pattern:
        - Start with the direct answer
        - Add 1-2 key details if needed
        - End with a question ONLY if you need specific information

        Examples of GOOD responses:
        "Oil changes cost LKR 5,000-8,000. Book online or call us."
        "We're open Mon-Sat, 8am-6pm. Sunday closed."
        "I need your preferred date and vehicle model to book the service."

        Examples of BAD responses:
        "Thank you for asking about our services. I'd be happy to help..."
        "Let me provide you with comprehensive information about..."
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
                    "options", buildLLMOptions()
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
            log.debug("Raw response length: {} chars", generatedResponse.length());

            // Post-process response to ensure conciseness
            String processedResponse = responseProcessor.processResponse(generatedResponse);
            log.debug("Processed response: {} chars, {} words",
                processedResponse.length(),
                responseProcessor.countWords(processedResponse));

            return processedResponse;
            
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
                    "options", buildLLMOptions()
                ))
                .retrieve()
                .bodyToMono(OllamaResponse.class)
                .block();

            if (response != null && response.getMessage() != null) {
                String rawResponse = response.getMessage().getContent();
                return responseProcessor.processResponse(rawResponse);
            }

            return getFallbackResponse();
                
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
            Context: %s

            Question: %s

            Answer concisely using the context. If information is missing, say so briefly.
            """, context, userQuery);
    }

    /**
     * Build LLM options map from configuration
     */
    private Map<String, Object> buildLLMOptions() {
        var params = ollamaProperties.getParameters();
        return Map.of(
            "temperature", params.getTemperature(),
            "top_p", params.getTopP(),
            "top_k", params.getTopK(),
            "num_predict", params.getMaxTokens(),
            "repeat_penalty", params.getRepeatPenalty()
        );
    }
    
    /**
     * Get fallback response when LLM fails
     */
    private String getFallbackResponse() {
        return "Sorry, I'm having technical difficulties. Please try again or call our support team.";
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
