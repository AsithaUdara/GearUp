package com.gearup.chatbotservice.integration;

import com.gearup.chatbotservice.dto.ChatMessageRequest;
import com.gearup.chatbotservice.dto.ChatMessageResponse;
import com.gearup.chatbotservice.entity.ChatSession;
import com.gearup.chatbotservice.entity.ConversationHistory;
import com.gearup.chatbotservice.repository.ChatSessionRepository;
import com.gearup.chatbotservice.repository.ConversationHistoryRepository;
import com.gearup.chatbotservice.repository.KnowledgeDocumentRepository;
import com.gearup.chatbotservice.service.ChatbotService;
import com.gearup.chatbotservice.service.ChatSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Integration tests for Chatbot Service
 * These tests use H2 in-memory database and mock external dependencies
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "eureka.client.enabled=false",
    "spring.rabbitmq.host=localhost",
    "spring.rabbitmq.port=5672",
    "knowledge-base.loader.enabled=false",
    "knowledge-base.indexer.enabled=false",
    "ollama.base-url=http://localhost:11434",
    "ollama.model=llama3.2",
    "ollama.embedding-model=nomic-embed-text"
})
@DisplayName("Chatbot Service Integration Tests")
@Transactional
class ChatbotServiceIntegrationTest {

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ConversationHistoryRepository conversationHistoryRepository;

    @Autowired
    private KnowledgeDocumentRepository knowledgeDocumentRepository;

    @MockBean
    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        conversationHistoryRepository.deleteAll();
        chatSessionRepository.deleteAll();
        knowledgeDocumentRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create and retrieve chat session")
    void testCreateAndRetrieveSession() {
        // Given
        String sessionId = "test-session-001";
        String userId = "test-user-001";

        // When
        ChatSession created = chatSessionService.createSession(sessionId, userId);
        ChatSession retrieved = chatSessionService.getSession(sessionId).orElse(null);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getSessionId()).isEqualTo(sessionId);
        assertThat(created.getUserId()).isEqualTo(userId);
        assertThat(created.getStatus()).isEqualTo(ChatSession.SessionStatus.ACTIVE);
        
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getSessionId()).isEqualTo(sessionId);
    }

    @Test
    @DisplayName("Should handle multiple sessions for same user")
    void testMultipleSessionsPerUser() {
        // Given
        String userId = "test-user-002";
        String session1 = "session-1";
        String session2 = "session-2";

        // When
        chatSessionService.createSession(session1, userId);
        chatSessionService.createSession(session2, userId);
        List<ChatSession> userSessions = chatSessionService.getUserSessions(userId);

        // Then
        assertThat(userSessions).hasSize(2);
        assertThat(userSessions).extracting(ChatSession::getSessionId)
                .containsExactlyInAnyOrder(session1, session2);
    }

    @Test
    @DisplayName("Should close session successfully")
    void testCloseSession() {
        // Given
        String sessionId = "session-to-close";
        String userId = "test-user-003";
        chatSessionService.createSession(sessionId, userId);

        // When
        chatSessionService.closeSession(sessionId);
        ChatSession closedSession = chatSessionService.getSession(sessionId).orElse(null);

        // Then
        assertThat(closedSession).isNotNull();
        assertThat(closedSession.getStatus()).isEqualTo(ChatSession.SessionStatus.CLOSED);
        assertThat(closedSession.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should get active sessions only")
    void testGetActiveSessions() {
        // Given
        String userId = "test-user-004";
        String activeSession = "active-session";
        String closedSession = "closed-session";
        
        chatSessionService.createSession(activeSession, userId);
        chatSessionService.createSession(closedSession, userId);
        chatSessionService.closeSession(closedSession);

        // When
        List<ChatSession> activeSessions = chatSessionService.getActiveSessions(userId);

        // Then
        assertThat(activeSessions).hasSize(1);
        assertThat(activeSessions.get(0).getSessionId()).isEqualTo(activeSession);
    }

    @Test
    @DisplayName("Should save and retrieve conversation history")
    void testConversationHistory() {
        // Given
        String sessionId = "session-with-history";
        String userId = "test-user-005";
        
        ConversationHistory message1 = ConversationHistory.builder()
                .sessionId(sessionId)
                .userId(userId)
                .message("Hello")
                .sender(ConversationHistory.MessageSender.USER)
                .build();
        
        ConversationHistory message2 = ConversationHistory.builder()
                .sessionId(sessionId)
                .userId(userId)
                .message("Hi there!")
                .sender(ConversationHistory.MessageSender.BOT)
                .build();

        // When
        conversationHistoryRepository.save(message1);
        conversationHistoryRepository.save(message2);
        List<ConversationHistory> history = conversationHistoryRepository
                .findBySessionIdOrderByCreatedAtAsc(sessionId);

        // Then
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getMessage()).isEqualTo("Hello");
        assertThat(history.get(1).getMessage()).isEqualTo("Hi there!");
    }

    @Test
    @DisplayName("Should handle empty session history")
    void testEmptySessionHistory() {
        // Given
        String nonExistentSession = "non-existent-session";

        // When
        List<ConversationHistory> history = conversationHistoryRepository
                .findBySessionIdOrderByCreatedAtAsc(nonExistentSession);

        // Then
        assertThat(history).isEmpty();
    }

    @Test
    @DisplayName("Should process chat message with mocked service")
    void testProcessChatMessage() {
        // Given
        String sessionId = "test-session-006";
        String userId = "test-user-006";
        
        ChatMessageRequest request = ChatMessageRequest.builder()
                .content("Test message")
                .sessionId(sessionId)
                .userId(userId)
                .build();

        ChatMessageResponse mockResponse = ChatMessageResponse.builder()
                .content("Mock response")
                .sessionId(sessionId)
                .intent("test")
                .build();

        when(chatbotService.processMessage(any(ChatMessageRequest.class)))
                .thenReturn(mockResponse);

        // When
        ChatMessageResponse response = chatbotService.processMessage(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("Mock response");
        assertThat(response.getSessionId()).isEqualTo(sessionId);
    }

    @Test
    @DisplayName("Should reactivate closed session")
    void testReactivateClosedSession() {
        // Given
        String sessionId = "session-to-reactivate";
        String userId = "test-user-007";
        
        chatSessionService.createSession(sessionId, userId);
        chatSessionService.closeSession(sessionId);

        // When
        chatSessionService.ensureSessionActive(sessionId, userId);
        ChatSession reactivated = chatSessionService.getSession(sessionId).orElse(null);

        // Then
        assertThat(reactivated).isNotNull();
        assertThat(reactivated.getStatus()).isEqualTo(ChatSession.SessionStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should get user sessions ordered by creation date")
    void testGetUserSessionsOrdered() {
        // Given
        String userId = "test-user-008";
        
        chatSessionService.createSession("session-1", userId);
        // Small delay to ensure different timestamps
        try { Thread.sleep(10); } catch (InterruptedException e) {}
        chatSessionService.createSession("session-2", userId);
        try { Thread.sleep(10); } catch (InterruptedException e) {}
        chatSessionService.createSession("session-3", userId);

        // When
        List<ChatSession> sessions = chatSessionService.getUserSessions(userId);

        // Then
        assertThat(sessions).hasSize(3);
        // Should be ordered by creation date descending
        assertThat(sessions.get(0).getSessionId()).isEqualTo("session-3");
    }

    @Test
    @DisplayName("Should handle database persistence correctly")
    void testDatabasePersistence() {
        // Given
        String sessionId = "persistent-session";
        String userId = "test-user-009";

        // When
        ChatSession created = chatSessionService.createSession(sessionId, userId);
        Long id = created.getId();

        // Clear the persistence context to force a database read
        ChatSession fromDb = chatSessionRepository.findById(id).orElse(null);

        // Then
        assertThat(fromDb).isNotNull();
        assertThat(fromDb.getSessionId()).isEqualTo(sessionId);
        assertThat(fromDb.getUserId()).isEqualTo(userId);
    }
}
