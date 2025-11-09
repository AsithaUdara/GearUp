package com.gearup.chatbotservice.repository;

import com.gearup.chatbotservice.entity.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for KnowledgeDocument entity
 * Provides vector similarity search capabilities
 */
@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {
    
    /**
     * Find documents by source
     * @param source the document source
     * @return list of documents from the source
     */
    List<KnowledgeDocument> findBySource(String source);
    
    /**
     * Find documents by category
     * @param category the document category
     * @return list of documents in the category
     */
    List<KnowledgeDocument> findByCategory(String category);
    
    /**
     * Find similar documents using vector cosine similarity
     * @param embedding the query embedding vector as string
     * @param limit maximum number of results
     * @return list of similar documents ordered by similarity
     */
    @Query(value = """
        SELECT * FROM knowledge_documents
        WHERE embedding IS NOT NULL
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<KnowledgeDocument> findSimilarDocuments(
        @Param("embedding") String embedding,
        @Param("limit") int limit
    );
    
    /**
     * Find similar documents with category filter
     * @param embedding the query embedding vector as string
     * @param category the category to filter by
     * @param limit maximum number of results
     * @return list of similar documents in the category
     */
    @Query(value = """
        SELECT * FROM knowledge_documents
        WHERE category = :category AND embedding IS NOT NULL
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<KnowledgeDocument> findSimilarDocumentsByCategory(
        @Param("embedding") String embedding,
        @Param("category") String category,
        @Param("limit") int limit
    );
    
    /**
     * Find similar documents with similarity threshold
     * @param embedding the query embedding vector as string
     * @param threshold similarity threshold (0-1, lower is more similar)
     * @param limit maximum number of results
     * @return list of similar documents above threshold
     */
    @Query(value = """
        SELECT * FROM knowledge_documents
        WHERE embedding IS NOT NULL 
          AND (embedding <=> CAST(:embedding AS vector)) < :threshold
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<KnowledgeDocument> findSimilarDocumentsWithThreshold(
        @Param("embedding") String embedding,
        @Param("threshold") double threshold,
        @Param("limit") int limit
    );
    
    /**
     * Delete all documents from a specific source
     * @param source the document source
     */
    void deleteBySource(String source);
}
