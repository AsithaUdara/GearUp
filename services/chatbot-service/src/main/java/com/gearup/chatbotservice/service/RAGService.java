package com.gearup.chatbotservice.service;

import com.gearup.chatbotservice.entity.KnowledgeDocument;
import com.gearup.chatbotservice.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG (Retrieval-Augmented Generation) Service
 * Implements the RAG pipeline: Query -> Embedding -> Retrieval -> Context Building -> Generation
 */
@Service
@ConditionalOnProperty(name = "rag.service.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
@RequiredArgsConstructor
public class RAGService {
    
    private final EmbeddingService embeddingService;
    private final KnowledgeDocumentRepository knowledgeRepository;
    private final OllamaService ollamaService;
    
    private static final int DEFAULT_TOP_K = 3;
    private static final double SIMILARITY_THRESHOLD = 0.7;
    
    /**
     * Process user query with RAG pipeline
     * 
     * @param userQuery user's question
     * @param userId user identifier
     * @return generated response with context
     */
    public String processQuery(String userQuery, String userId) {
        return processQuery(userQuery, userId, DEFAULT_TOP_K);
    }
    
    /**
     * Process user query with custom top-k
     * 
     * @param userQuery user's question
     * @param userId user identifier
     * @param topK number of documents to retrieve
     * @return generated response with context
     */
    public String processQuery(String userQuery, String userId, int topK) {
        log.info("Processing RAG query from user {}: {}", userId, userQuery);
        
        try {
            // Step 1: Generate query embedding
            log.debug("Step 1: Generating query embedding");
            float[] queryEmbedding = embeddingService.generateEmbedding(userQuery);
            
            // Step 2: Retrieve relevant documents
            log.debug("Step 2: Retrieving top {} similar documents", topK);
            String embeddingStr = convertEmbeddingToString(queryEmbedding);
            List<KnowledgeDocument> relevantDocs = 
                knowledgeRepository.findSimilarDocuments(embeddingStr, topK);
            
            // Step 3: Build context from retrieved documents
            String context = buildContext(relevantDocs);
            
            if (context.isBlank()) {
                log.warn("No relevant documents found for query: {}", userQuery);
                // Still process with LLM, but without context
                return ollamaService.generateResponse(userQuery, "");
            }
            
            log.debug("Step 3: Built context from {} documents", relevantDocs.size());
            
            // Step 4: Generate response using LLM with context
            log.debug("Step 4: Generating response with LLM");
            String response = ollamaService.generateResponse(userQuery, context);
            
            log.info("RAG response generated successfully for user {}", userId);
            return response;
            
        } catch (Exception e) {
            log.error("Error processing RAG query", e);
            return "Sorry, I encountered an error. Please try again or contact support.";
        }
    }
    
    /**
     * Process query with category filter
     * 
     * @param userQuery user's question
     * @param userId user identifier
     * @param category category to filter documents
     * @return generated response
     */
    public String processQueryWithCategory(String userQuery, String userId, String category) {
        log.info("Processing RAG query with category filter: {}", category);
        
        try {
            float[] queryEmbedding = embeddingService.generateEmbedding(userQuery);
            String embeddingStr = convertEmbeddingToString(queryEmbedding);
            
            List<KnowledgeDocument> relevantDocs = 
                knowledgeRepository.findSimilarDocumentsByCategory(
                    embeddingStr, category, DEFAULT_TOP_K);
            
            String context = buildContext(relevantDocs);
            return ollamaService.generateResponse(userQuery, context);
            
        } catch (Exception e) {
            log.error("Error processing RAG query with category", e);
            return ollamaService.generateResponse(userQuery, "");
        }
    }
    
    /**
     * Build context string from retrieved documents
     * 
     * @param documents list of knowledge documents
     * @return formatted context string
     */
    private String buildContext(List<KnowledgeDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return "";
        }
        
        return documents.stream()
            .map(doc -> String.format(
                "Source: %s\nCategory: %s\nContent: %s",
                doc.getSource(),
                doc.getCategory() != null ? doc.getCategory() : "General",
                doc.getContent()
            ))
            .collect(Collectors.joining("\n\n---\n\n"));
    }
    
    /**
     * Get relevant documents without generating response
     * Useful for showing sources to users
     * 
     * @param query search query
     * @param topK number of documents
     * @return list of relevant documents
     */
    public List<KnowledgeDocument> getRelevantDocuments(String query, int topK) {
        try {
            float[] queryEmbedding = embeddingService.generateEmbedding(query);
            String embeddingStr = convertEmbeddingToString(queryEmbedding);
            return knowledgeRepository.findSimilarDocuments(embeddingStr, topK);
        } catch (Exception e) {
            log.error("Error retrieving relevant documents", e);
            return List.of();
        }
    }
    
    /**
     * Helper method to convert float array to PostgreSQL vector string format
     */
    private String convertEmbeddingToString(float[] embedding) {
        if (embedding == null) return null;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
