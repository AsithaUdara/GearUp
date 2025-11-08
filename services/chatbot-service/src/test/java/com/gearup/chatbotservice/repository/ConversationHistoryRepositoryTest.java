package com.gearup.chatbotservice.repository;

import com.gearup.chatbotservice.config.TestRabbitMQConfig;
import com.gearup.chatbotservice.entity.ConversationHistory;
import com.gearup.chatbotservice.entity.ConversationHistory.MessageSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for ConversationHistory
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(TestRabbitMQConfig.class)
@DisplayName("ConversationHistoryRepository Tests")
class ConversationHistoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ConversationHistoryRepository repository;

    @Test
    @DisplayName("Should save and retrieve conversation history")
    void testSaveAndFind() {
        // Given
        ConversationHistory message = ConversationHistory.builder()
                .sessionId("session-123")
                .userId("user-456")
                .message("Hello, chatbot!")
                .sender(MessageSender.USER)
                .intent("greeting")
                .build();

        // When
        ConversationHistory saved = repository.save(message);
        entityManager.flush();
        ConversationHistory found = repository.findById(saved.getId()).orElse(null);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getSessionId()).isEqualTo("session-123");
        assertThat(found.getMessage()).isEqualTo("Hello, chatbot!");
        assertThat(found.getSender()).isEqualTo(MessageSender.USER);
    }

    @Test
    @DisplayName("Should find messages by sessionId ordered by createdAt")
    void testFindBySessionIdOrderByCreatedAtAsc() {
        // Given
        ConversationHistory msg1 = ConversationHistory.builder()
                .sessionId("session-1")
                .userId("user1")
                .message("First message")
                .sender(MessageSender.USER)
                .build();

        ConversationHistory msg2 = ConversationHistory.builder()
                .sessionId("session-1")
                .userId("user1")
                .message("Second message")
                .sender(MessageSender.BOT)
                .build();

        repository.save(msg1);
        entityManager.flush();
        // Small delay to ensure different timestamps
        try { Thread.sleep(10); } catch (InterruptedException e) { }
        repository.save(msg2);
        entityManager.flush();

        // When
        List<ConversationHistory> messages = repository.findBySessionIdOrderByCreatedAtAsc("session-1");

        // Then
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getMessage()).isEqualTo("First message");
        assertThat(messages.get(1).getMessage()).isEqualTo("Second message");
    }

    @Test
    @DisplayName("Should find messages by userId with pagination")
    void testFindByUserId() {
        // Given
        for (int i = 0; i < 15; i++) {
            ConversationHistory msg = ConversationHistory.builder()
                    .sessionId("session-" + i)
                    .userId("user1")
                    .message("Message " + i)
                    .sender(MessageSender.USER)
                    .build();
            repository.save(msg);
        }
        entityManager.flush();

        // When
        Page<ConversationHistory> page = repository.findByUserId("user1", PageRequest.of(0, 10));

        // Then
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(15);
    }

    @Test
    @DisplayName("Should save conversation with different senders")
    void testDifferentSenders() {
        // Given
        ConversationHistory userMsg = ConversationHistory.builder()
                .sessionId("session-1")
                .userId("user1")
                .message("User message")
                .sender(MessageSender.USER)
                .build();

        ConversationHistory botMsg = ConversationHistory.builder()
                .sessionId("session-1")
                .userId("user1")
                .message("Bot response")
                .sender(MessageSender.BOT)
                .build();

        ConversationHistory systemMsg = ConversationHistory.builder()
                .sessionId("session-1")
                .userId("user1")
                .message("System notification")
                .sender(MessageSender.SYSTEM)
                .build();

        // When
        repository.save(userMsg);
        repository.save(botMsg);
        repository.save(systemMsg);
        entityManager.flush();

        List<ConversationHistory> messages = repository.findBySessionIdOrderByCreatedAtAsc("session-1");

        // Then
        assertThat(messages).hasSize(3);
        assertThat(messages).extracting(ConversationHistory::getSender)
                .containsExactly(MessageSender.USER, MessageSender.BOT, MessageSender.SYSTEM);
    }

    @Test
    @DisplayName("Should save message with intent")
    void testSaveWithIntent() {
        // Given
        ConversationHistory message = ConversationHistory.builder()
                .sessionId("session-1")
                .userId("user1")
                .message("I want to book a service")
                .sender(MessageSender.USER)
                .intent("booking")
                .build();

        // When
        ConversationHistory saved = repository.save(message);
        entityManager.flush();
        entityManager.clear();

        ConversationHistory found = repository.findById(saved.getId()).orElse(null);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getIntent()).isEqualTo("booking");
    }

    @Test
    @DisplayName("Should save message with metadata")
    void testSaveWithMetadata() {
        // Given
        ConversationHistory message = ConversationHistory.builder()
                .sessionId("session-1")
                .userId("user1")
                .message("Test message")
                .sender(MessageSender.USER)
                .metadata("{\"confidence\": 0.95, \"source\": \"web\"}")
                .build();

        // When
        ConversationHistory saved = repository.save(message);
        entityManager.flush();
        entityManager.clear();

        ConversationHistory found = repository.findById(saved.getId()).orElse(null);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getMetadata()).contains("confidence");
    }

    @Test
    @DisplayName("Should return empty list for non-existent session")
    void testFindByNonExistentSession() {
        // When
        List<ConversationHistory> messages = repository.findBySessionIdOrderByCreatedAtAsc("non-existent");

        // Then
        assertThat(messages).isEmpty();
    }

    @Test
    @DisplayName("Should handle long messages")
    void testLongMessage() {
        // Given
        String longMessage = "a".repeat(5000);
        ConversationHistory message = ConversationHistory.builder()
                .sessionId("session-1")
                .userId("user1")
                .message(longMessage)
                .sender(MessageSender.USER)
                .build();

        // When
        ConversationHistory saved = repository.save(message);
        entityManager.flush();

        // Then
        assertThat(saved.getMessage()).hasSize(5000);
    }
}
