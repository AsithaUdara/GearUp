package com.gearup.chatbotservice.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import com.gearup.chatbotservice.config.VectorType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a knowledge base document with vector embeddings
 * Used for RAG (Retrieval-Augmented Generation) capabilities
 */
@Entity
@Table(name = "knowledge_documents", indexes = {
    @Index(name = "idx_knowledge_documents_source", columnList = "source"),
    @Index(name = "idx_knowledge_documents_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;
    
    @Column(nullable = false)
    private String source;
    
    @Column(length = 100)
    private String category;
    
    @Type(VectorType.class)
    @Column(columnDefinition = "vector(768)")
    private String embedding;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Sets the embedding from a float array
     * @param embeddingArray float array representing the embedding vector
     */
    public void setEmbeddingFromArray(float[] embeddingArray) {
        if (embeddingArray == null) {
            this.embedding = null;
            return;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embeddingArray.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embeddingArray[i]);
        }
        sb.append("]");
        this.embedding = sb.toString();
    }
    
    /**
     * Gets the embedding as a float array
     * @return float array representing the embedding vector
     */
    public float[] getEmbeddingAsArray() {
        if (this.embedding == null || this.embedding.isEmpty()) {
            return null;
        }
        
        String cleaned = this.embedding.replaceAll("[\\[\\]]", "");
        String[] parts = cleaned.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        return result;
    }
}
