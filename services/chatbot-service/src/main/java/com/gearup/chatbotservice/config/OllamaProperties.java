package com.gearup.chatbotservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuration properties for Ollama integration
 */
@Configuration
@ConfigurationProperties(prefix = "ollama")
@Data
public class OllamaProperties {

    /**
     * Base URL for Ollama API
     */
    private String baseUrl = "http://localhost:11434";

    /**
     * Chat model to use
     */
    private String chatModel = "llama3.2:3b";

    /**
     * Embedding model to use
     */
    private String embeddingModel = "nomic-embed-text";

    /**
     * Request timeout in milliseconds
     */
    private int timeout = 60000;

    /**
     * Maximum retries for failed requests
     */
    private int maxRetries = 3;

    /**
     * LLM generation parameters
     */
    private LLMParameters parameters = new LLMParameters();

    @Data
    public static class LLMParameters {
        /**
         * Temperature for response generation (0.0 = deterministic, 1.0 = creative)
         * Lower values produce more focused, concise responses
         */
        private double temperature = 0.3;

        /**
         * Top-p sampling (nucleus sampling)
         * Lower values = more focused responses
         */
        private double topP = 0.7;

        /**
         * Top-k sampling
         * Lower values = more focused vocabulary selection
         */
        private int topK = 20;

        /**
         * Maximum tokens in response
         * Helps enforce conciseness
         */
        private int maxTokens = 200;

        /**
         * Repeat penalty (higher = less repetition)
         */
        private double repeatPenalty = 1.2;
    }
}
