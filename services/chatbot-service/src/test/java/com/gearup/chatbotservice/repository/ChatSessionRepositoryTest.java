package com.gearup.chatbotservice.repository;

import com.gearup.chatbotservice.config.TestRabbitMQConfig;
import com.gearup.chatbotservice.entity.ChatSession;
import com.gearup.chatbotservice.entity.ChatSession.SessionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for ChatSession
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(TestRabbitMQConfig.class)
@DisplayName("ChatSessionRepository Tests")
class ChatSessionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChatSessionRepository repository;

    @Test
    @DisplayName("Should save and retrieve chat session")
    void testSaveAndFind() {
        // Given
        ChatSession session = ChatSession.builder()
                .sessionId("session-123")
                .userId("user-456")
                .status(SessionStatus.ACTIVE)
                .build();

        // When
        ChatSession saved = repository.save(session);
        entityManager.flush();
        Optional<ChatSession> found = repository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getSessionId()).isEqualTo("session-123");
        assertThat(found.get().getUserId()).isEqualTo("user-456");
        assertThat(found.get().getStatus()).isEqualTo(SessionStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should find session by sessionId")
    void testFindBySessionId() {
        // Given
        ChatSession session = ChatSession.builder()
                .sessionId("unique-session-id")
                .userId("user1")
                .status(SessionStatus.ACTIVE)
                .build();

        repository.save(session);
        entityManager.flush();

        // When
        Optional<ChatSession> found = repository.findBySessionId("unique-session-id");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getSessionId()).isEqualTo("unique-session-id");
    }

    @Test
    @DisplayName("Should find sessions by userId and status")
    void testFindByUserIdAndStatus() {
        // Given
        ChatSession active1 = ChatSession.builder()
                .sessionId("session-1")
                .userId("user1")
                .status(SessionStatus.ACTIVE)
                .build();

        ChatSession active2 = ChatSession.builder()
                .sessionId("session-2")
                .userId("user1")
                .status(SessionStatus.ACTIVE)
                .build();

        ChatSession closed = ChatSession.builder()
                .sessionId("session-3")
                .userId("user1")
                .status(SessionStatus.CLOSED)
                .build();

        repository.save(active1);
        repository.save(active2);
        repository.save(closed);
        entityManager.flush();

        // When
        List<ChatSession> activeSessions = repository.findByUserIdAndStatus("user1", SessionStatus.ACTIVE);

        // Then
        assertThat(activeSessions).hasSize(2);
        assertThat(activeSessions).allMatch(s -> s.getStatus() == SessionStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should find sessions by userId ordered by createdAt desc")
    void testFindByUserIdOrderByCreatedAtDesc() {
        // Given
        ChatSession session1 = ChatSession.builder()
                .sessionId("session-1")
                .userId("user1")
                .status(SessionStatus.ACTIVE)
                .build();

        ChatSession session2 = ChatSession.builder()
                .sessionId("session-2")
                .userId("user1")
                .status(SessionStatus.ACTIVE)
                .build();

        repository.save(session1);
        entityManager.flush();
        // Small delay to ensure different timestamps
        try { Thread.sleep(10); } catch (InterruptedException e) { }
        repository.save(session2);
        entityManager.flush();

        // When
        List<ChatSession> sessions = repository.findByUserIdOrderByCreatedAtDesc("user1");

        // Then
        assertThat(sessions).hasSize(2);
        // Most recent first
        assertThat(sessions.get(0).getSessionId()).isEqualTo("session-2");
    }

    @Test
    @DisplayName("Should count active sessions by user")
    void testCountActiveSessionsByUser() {
        // Given
        repository.save(ChatSession.builder()
                .sessionId("s1").userId("user1").status(SessionStatus.ACTIVE).build());
        repository.save(ChatSession.builder()
                .sessionId("s2").userId("user1").status(SessionStatus.ACTIVE).build());
        repository.save(ChatSession.builder()
                .sessionId("s3").userId("user1").status(SessionStatus.CLOSED).build());
        entityManager.flush();

        // When
        long count = repository.countActiveSessionsByUser("user1");

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should update session status")
    void testUpdateStatus() {
        // Given
        ChatSession session = ChatSession.builder()
                .sessionId("session-to-close")
                .userId("user1")
                .status(SessionStatus.ACTIVE)
                .build();

        ChatSession saved = repository.save(session);
        entityManager.flush();

        // When
        saved.setStatus(SessionStatus.CLOSED);
        saved.setClosedAt(LocalDateTime.now());
        repository.save(saved);
        entityManager.flush();
        entityManager.clear();

        Optional<ChatSession> updated = repository.findById(saved.getId());

        // Then
        assertThat(updated).isPresent();
        assertThat(updated.get().getStatus()).isEqualTo(SessionStatus.CLOSED);
        assertThat(updated.get().getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find stale sessions")
    void testFindByUpdatedAtBeforeAndStatus() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        ChatSession oldSession = ChatSession.builder()
                .sessionId("old-session")
                .userId("user1")
                .status(SessionStatus.ACTIVE)
                .build();

        repository.save(oldSession);
        entityManager.flush();

        // When
        List<ChatSession> staleSessions = repository.findByUpdatedAtBeforeAndStatus(
                now.plusHours(1), SessionStatus.ACTIVE);

        // Then
        assertThat(staleSessions).isNotEmpty();
    }

    @Test
    @DisplayName("Should check if session exists and is active")
    void testExistsBySessionIdAndStatus() {
        // Given
        ChatSession session = ChatSession.builder()
                .sessionId("check-session")
                .userId("user1")
                .status(SessionStatus.ACTIVE)
                .build();

        repository.save(session);
        entityManager.flush();

        // When
        boolean exists = repository.existsBySessionIdAndStatus("check-session", SessionStatus.ACTIVE);
        boolean notExists = repository.existsBySessionIdAndStatus("check-session", SessionStatus.CLOSED);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
