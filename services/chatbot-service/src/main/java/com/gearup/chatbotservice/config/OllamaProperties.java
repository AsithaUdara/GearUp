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
}
