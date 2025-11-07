package com.gearup.chatbotservice.service;

import com.gearup.chatbotservice.entity.ChatSession;
import com.gearup.chatbotservice.entity.ChatSession.SessionStatus;
import com.gearup.chatbotservice.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Chat Session Management Service
 * Manages chat session lifecycle and state
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatSessionService {
    
    private final ChatSessionRepository sessionRepository;
    
    private static final int MAX_ACTIVE_SESSIONS_PER_USER = 5;
    private static final int SESSION_TIMEOUT_HOURS = 24;
    
    /**
     * Create a new chat session
     * 
     * @param sessionId unique session identifier
     * @param userId user identifier
     * @return created chat session
     */
    @Transactional
    public ChatSession createSession(String sessionId, String userId) {
        
        // Check if session already exists
        Optional<ChatSession> existing = sessionRepository.findBySessionId(sessionId);
        if (existing.isPresent()) {
            log.warn("Session already exists: {}", sessionId);
            return existing.get();
        }
        
        // Check user's active session count
        long activeCount = sessionRepository.countActiveSessionsByUser(userId);
        if (activeCount >= MAX_ACTIVE_SESSIONS_PER_USER) {
            log.warn("User {} has reached maximum active sessions", userId);
            closeOldestSession(userId);
        }
        
        ChatSession session = ChatSession.builder()
            .sessionId(sessionId)
            .userId(userId)
            .status(SessionStatus.ACTIVE)
            .build();
        
        ChatSession saved = sessionRepository.save(session);
        log.info("Created new chat session: {} for user: {}", sessionId, userId);
        
        return saved;
    }
    
    /**
     * Get session by ID
     * 
     * @param sessionId session identifier
     * @return optional chat session
     */
    public Optional<ChatSession> getSession(String sessionId) {
        return sessionRepository.findBySessionId(sessionId);
    }
    
    /**
     * Ensure session is active, create if not exists
     * 
     * @param sessionId session identifier
     * @param userId user identifier
     */
    @Transactional
    public void ensureSessionActive(String sessionId, String userId) {
        Optional<ChatSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
        
        if (sessionOpt.isEmpty()) {
            createSession(sessionId, userId);
        } else {
            ChatSession session = sessionOpt.get();
            if (session.getStatus() != SessionStatus.ACTIVE) {
                session.setStatus(SessionStatus.ACTIVE);
                sessionRepository.save(session);
                log.info("Reactivated session: {}", sessionId);
            }
        }
    }
    
    /**
     * Close a session
     * 
     * @param sessionId session identifier
     */
    @Transactional
    public void closeSession(String sessionId) {
        sessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            session.setStatus(SessionStatus.CLOSED);
            session.setClosedAt(LocalDateTime.now());
            sessionRepository.save(session);
            log.info("Closed session: {}", sessionId);
        });
    }
    
    /**
     * Get active sessions for a user
     * 
     * @param userId user identifier
     * @return list of active sessions
     */
    public List<ChatSession> getActiveSessions(String userId) {
        return sessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE);
    }
    
    /**
     * Get all sessions for a user
     * 
     * @param userId user identifier
     * @return list of all sessions
     */
    public List<ChatSession> getUserSessions(String userId) {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Close oldest active session for user
     */
    private void closeOldestSession(String userId) {
        List<ChatSession> activeSessions = getActiveSessions(userId);
        if (!activeSessions.isEmpty()) {
            ChatSession oldest = activeSessions.get(activeSessions.size() - 1);
            closeSession(oldest.getSessionId());
        }
    }
    
    /**
     * Archive stale sessions (cleanup task)
     * Should be run periodically
     */
    @Transactional
    public void archiveStaleSessions() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(SESSION_TIMEOUT_HOURS);
        
        List<ChatSession> staleSessions = sessionRepository
            .findByUpdatedAtBeforeAndStatus(threshold, SessionStatus.ACTIVE);
        
        staleSessions.forEach(session -> {
            session.setStatus(SessionStatus.ARCHIVED);
            sessionRepository.save(session);
        });
        
        log.info("Archived {} stale sessions", staleSessions.size());
    }
}
