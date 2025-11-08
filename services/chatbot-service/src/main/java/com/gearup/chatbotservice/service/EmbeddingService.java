package com.gearup.chatbotservice.service;

import com.gearup.chatbotservice.config.OllamaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

/**
 * Service for generating text embeddings using Ollama
 * Embeddings are used for semantic similarity search in RAG
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmbeddingService {
    
    @Qualifier("ollamaWebClient")
    private final WebClient ollamaWebClient;
    
    private final OllamaProperties ollamaProperties;
    
    /**
     * Generate embedding vector for given text
     * 
     * @param text input text to embed
     * @return embedding vector as float array
     * @throws RuntimeException if embedding generation fails
     */
    public float[] generateEmbedding(String text) {
        try {
            log.debug("Generating embedding for text: {}", 
                text.length() > 100 ? text.substring(0, 100) + "..." : text);
            
            float[] embedding = generateEmbeddingArray(text);
            
            if (embedding == null || embedding.length == 0) {
                throw new RuntimeException("Generated embedding is empty");
            }
            
            log.debug("Successfully generated embedding with dimension: {}", embedding.length);
            return embedding;
            
        } catch (Exception e) {
            log.error("Failed to generate embedding for text", e);
            throw new RuntimeException("Embedding generation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate embedding as float array
     * 
     * @param text input text to embed
     * @return embedding as float array
     */
    public float[] generateEmbeddingArray(String text) {
        try {
            EmbeddingResponse response = ollamaWebClient.post()
                .uri("/api/embeddings")
                .bodyValue(Map.of(
                    "model", ollamaProperties.getEmbeddingModel(),
                    "prompt", text
                ))
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)
                .retryWhen(Retry.backoff(ollamaProperties.getMaxRetries(), Duration.ofSeconds(1))
                    .doBeforeRetry(signal -> 
                        log.warn("Retrying embedding generation, attempt: {}", signal.totalRetries() + 1)))
                .block();
            
            if (response == null || response.getEmbedding() == null) {
                throw new RuntimeException("Null response from Ollama");
            }
            
            return response.getEmbedding();
            
        } catch (Exception e) {
            log.error("Failed to generate embedding array", e);
            throw new RuntimeException("Embedding generation failed", e);
        }
    }
    
    /**
     * Response DTO for Ollama embedding API
     */
    @lombok.Data
    private static class EmbeddingResponse {
        private float[] embedding;
    }
}
