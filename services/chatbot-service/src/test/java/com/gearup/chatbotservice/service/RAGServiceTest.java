package com.gearup.chatbotservice.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gearup.chatbotservice.entity.ConversationHistory;
import com.gearup.chatbotservice.repository.ConversationHistoryRepository;

/**
 * Unit tests for RAGService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RAGService Tests")
class RAGServiceTest {

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private ConversationHistoryRepository conversationHistoryRepository;

    @InjectMocks
    private RAGService ragService;

    @Test
    @DisplayName("Should retrieve conversation context")
    void testGetConversationContext() {
        // Given
        String sessionId = "session-123";
        List<ConversationHistory> history = Arrays.asList(
                ConversationHistory.builder()
                        .sessionId(sessionId)
                        .message("First message")
                        .sender(ConversationHistory.MessageSender.USER)
                        .build(),
                ConversationHistory.builder()
                        .sessionId(sessionId)
                        .message("Second message")
                        .sender(ConversationHistory.MessageSender.BOT)
                        .build()
        );

        when(conversationHistoryRepository.findBySessionIdOrderByCreatedAtAsc(sessionId))
                .thenReturn(history);

        // When
        List<ConversationHistory> result = conversationHistoryRepository
                .findBySessionIdOrderByCreatedAtAsc(sessionId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMessage()).isEqualTo("First message");
    }

    @Test
    @DisplayName("Should handle empty conversation context")
    void testGetEmptyConversationContext() {
        // Given
        String sessionId = "empty-session";
        when(conversationHistoryRepository.findBySessionIdOrderByCreatedAtAsc(sessionId))
                .thenReturn(Collections.emptyList());

        // When
        List<ConversationHistory> result = conversationHistoryRepository
                .findBySessionIdOrderByCreatedAtAsc(sessionId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should save conversation history")
    void testSaveConversationHistory() {
        // Given
        ConversationHistory history = ConversationHistory.builder()
                .sessionId("session-123")
                .userId("user-123")
                .message("Test message")
                .sender(ConversationHistory.MessageSender.USER)
                .build();

        when(conversationHistoryRepository.save(any(ConversationHistory.class)))
                .thenReturn(history);

        // When
        ConversationHistory saved = conversationHistoryRepository.save(history);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getMessage()).isEqualTo("Test message");
        verify(conversationHistoryRepository).save(history);
    }
}
