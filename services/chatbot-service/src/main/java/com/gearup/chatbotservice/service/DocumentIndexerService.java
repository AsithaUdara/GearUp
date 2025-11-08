package com.gearup.chatbotservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gearup.chatbotservice.entity.KnowledgeDocument;
import com.gearup.chatbotservice.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Document Indexer Service
 * Indexes documents into the knowledge base with embeddings
 */
@Service
@ConditionalOnProperty(name = "knowledge-base.indexer.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
@RequiredArgsConstructor
public class DocumentIndexerService {
    
    private final EmbeddingService embeddingService;
    private final KnowledgeDocumentRepository repository;
    private final ObjectMapper objectMapper;
    
    private static final int MAX_CHUNK_WORDS = 300;
    
    /**
     * Index a document into the knowledge base
     * 
     * @param content document content
     * @param metadata document metadata
     * @param source document source
     * @param category document category
     */
    @Transactional
    public void indexDocument(String content, Map<String, Object> metadata, 
                             String source, String category) {
        try {
            log.info("Indexing document from source: {}, category: {}", source, category);
            
            // Split into chunks if too long
            List<String> chunks = chunkText(content, MAX_CHUNK_WORDS);
            log.debug("Split document into {} chunks", chunks.size());
            
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                
                // Generate embedding
                float[] embeddingArray = embeddingService.generateEmbedding(chunk);
                
                // Add chunk info to metadata
                Map<String, Object> chunkMetadata = new java.util.HashMap<>(metadata);
                if (chunks.size() > 1) {
                    chunkMetadata.put("chunk_index", i);
                    chunkMetadata.put("total_chunks", chunks.size());
                }
                
                // Create document
                KnowledgeDocument doc = KnowledgeDocument.builder()
                    .content(chunk)
                    .metadata(serializeMetadata(chunkMetadata))
                    .source(source)
                    .category(category)
                    .build();
                
                // Set embedding from array
                doc.setEmbeddingFromArray(embeddingArray);
                
                repository.save(doc);
            }
            
            log.info("Successfully indexed {} chunks from source: {}", chunks.size(), source);
            
        } catch (Exception e) {
            log.error("Failed to index document from source: {}", source, e);
            throw new RuntimeException("Document indexing failed", e);
        }
    }
    
    /**
     * Index multiple documents in batch
     */
    @Transactional
    public void indexDocuments(List<DocumentData> documents) {
        log.info("Indexing {} documents in batch", documents.size());
        
        for (DocumentData doc : documents) {
            indexDocument(doc.content, doc.metadata, doc.source, doc.category);
        }
    }
    
    /**
     * Delete documents by source
     */
    @Transactional
    public void deleteDocumentsBySource(String source) {
        repository.deleteBySource(source);
        log.info("Deleted documents from source: {}", source);
    }
    
    /**
     * Chunk text into smaller pieces
     */
    private List<String> chunkText(String text, int maxWords) {
        List<String> chunks = new ArrayList<>();
        String[] words = text.split("\\s+");
        
        for (int i = 0; i < words.length; i += maxWords) {
            int end = Math.min(i + maxWords, words.length);
            StringBuilder chunk = new StringBuilder();
            
            for (int j = i; j < end; j++) {
                chunk.append(words[j]).append(" ");
            }
            
            chunks.add(chunk.toString().trim());
        }
        
        return chunks;
    }
    
    /**
     * Serialize metadata to JSON string
     */
    private String serializeMetadata(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize metadata, using empty object", e);
            return "{}";
        }
    }
    
    /**
     * Data class for batch indexing
     */
    public static class DocumentData {
        public String content;
        public Map<String, Object> metadata;
        public String source;
        public String category;
        
        public DocumentData(String content, String source, String category) {
            this.content = content;
            this.source = source;
            this.category = category;
            this.metadata = Map.of();
        }
        
        public DocumentData(String content, Map<String, Object> metadata, 
                          String source, String category) {
            this.content = content;
            this.metadata = metadata;
            this.source = source;
            this.category = category;
        }
    }
}
