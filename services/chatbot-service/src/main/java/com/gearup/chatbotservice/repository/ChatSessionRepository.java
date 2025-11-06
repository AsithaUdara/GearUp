package com.gearup.chatbotservice.repository;

import com.gearup.chatbotservice.entity.ChatSession;
import com.gearup.chatbotservice.entity.ChatSession.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ChatSession entity
 * Manages chat session lifecycle and queries
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    
    /**
     * Find session by session ID
     * @param sessionId the session ID
     * @return optional chat session
     */
    Optional<ChatSession> findBySessionId(String sessionId);
    
    /**
     * Find active sessions for a user
     * @param userId the user ID
     * @return list of active sessions
     */
    List<ChatSession> findByUserIdAndStatus(String userId, SessionStatus status);
    
    /**
     * Find all sessions for a user
     * @param userId the user ID
     * @return list of all user sessions
     */
    List<ChatSession> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * Find sessions by status
     * @param status the session status
     * @return list of sessions with the status
     */
    List<ChatSession> findByStatus(SessionStatus status);
    
    /**
     * Find stale sessions (inactive for long period)
     * @param lastActivityBefore sessions not updated since this time
     * @param status current status
     * @return list of stale sessions
     */
    List<ChatSession> findByUpdatedAtBeforeAndStatus(
        LocalDateTime lastActivityBefore, 
        SessionStatus status
    );
    
    /**
     * Count active sessions for a user
     * @param userId the user ID
     * @return number of active sessions
     */
    @Query("SELECT COUNT(s) FROM ChatSession s WHERE s.userId = :userId AND s.status = 'ACTIVE'")
    long countActiveSessionsByUser(@Param("userId") String userId);
    
    /**
     * Check if session exists and is active
     * @param sessionId the session ID
     * @return true if session exists and is active
     */
    boolean existsBySessionIdAndStatus(String sessionId, SessionStatus status);
}
