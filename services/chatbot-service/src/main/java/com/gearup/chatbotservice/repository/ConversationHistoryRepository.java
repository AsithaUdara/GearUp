package com.gearup.chatbotservice.repository;

import com.gearup.chatbotservice.entity.ConversationHistory;
import com.gearup.chatbotservice.entity.ConversationHistory.MessageSender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ConversationHistory entity
 * Provides conversation tracking and retrieval capabilities
 */
@Repository
public interface ConversationHistoryRepository extends JpaRepository<ConversationHistory, Long> {
    
    /**
     * Find all messages in a session
     * @param sessionId the session ID
     * @return list of messages ordered by creation time
     */
    List<ConversationHistory> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    
    /**
     * Find messages by user ID
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of messages
     */
    Page<ConversationHistory> findByUserId(String userId, Pageable pageable);
    
    /**
     * Find recent messages in a session
     * @param sessionId the session ID
     * @param limit maximum number of messages
     * @return list of recent messages
     */
    @Query(value = """
        SELECT * FROM conversation_history
        WHERE session_id = :sessionId
        ORDER BY created_at DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<ConversationHistory> findRecentMessagesBySession(
        @Param("sessionId") String sessionId,
        @Param("limit") int limit
    );
    
    /**
     * Find messages by sender type
     * @param sessionId the session ID
     * @param sender the message sender
     * @return list of messages from the sender
     */
    List<ConversationHistory> findBySessionIdAndSender(String sessionId, MessageSender sender);
    
    /**
     * Find messages by intent
     * @param intent the intent classifier
     * @param pageable pagination information
     * @return page of messages with the intent
     */
    Page<ConversationHistory> findByIntent(String intent, Pageable pageable);
    
    /**
     * Count messages in a session
     * @param sessionId the session ID
     * @return number of messages
     */
    long countBySessionId(String sessionId);
    
    /**
     * Find messages within a time range
     * @param sessionId the session ID
     * @param start start time
     * @param end end time
     * @return list of messages in the time range
     */
    List<ConversationHistory> findBySessionIdAndCreatedAtBetween(
        String sessionId, 
        LocalDateTime start, 
        LocalDateTime end
    );
    
    /**
     * Delete old messages (for cleanup/archival)
     * @param before delete messages older than this date
     */
    void deleteByCreatedAtBefore(LocalDateTime before);
}
