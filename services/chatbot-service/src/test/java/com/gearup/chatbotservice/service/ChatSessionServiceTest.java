package com.gearup.chatbotservice.service;

import com.gearup.chatbotservice.entity.ChatSession;
import com.gearup.chatbotservice.entity.ChatSession.SessionStatus;
import com.gearup.chatbotservice.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChatSessionService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatSessionService Tests")
class ChatSessionServiceTest {

    @Mock
    private ChatSessionRepository sessionRepository;

    @InjectMocks
    private ChatSessionService chatSessionService;

    @Test
    @DisplayName("Should create new session")
    void testCreateSession() {
        // Given
        String sessionId = "session-123";
        String userId = "user-456";

        ChatSession expectedSession = ChatSession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .status(SessionStatus.ACTIVE)
                .build();

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());
        when(sessionRepository.countActiveSessionsByUser(userId)).thenReturn(0L);
        when(sessionRepository.save(any(ChatSession.class))).thenReturn(expectedSession);

        // When
        ChatSession result = chatSessionService.createSession(sessionId, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSessionId()).isEqualTo(sessionId);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        
        verify(sessionRepository).save(any(ChatSession.class));
    }

    @Test
    @DisplayName("Should return existing session if already exists")
    void testCreateSessionAlreadyExists() {
        // Given
        String sessionId = "existing-session";
        String userId = "user-123";

        ChatSession existingSession = ChatSession.builder()
                .id(1L)
                .sessionId(sessionId)
                .userId(userId)
                .status(SessionStatus.ACTIVE)
                .build();

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(existingSession));

        // When
        ChatSession result = chatSessionService.createSession(sessionId, userId);

        // Then
        assertThat(result).isEqualTo(existingSession);
        verify(sessionRepository, never()).save(any(ChatSession.class));
    }

    @Test
    @DisplayName("Should get session by sessionId")
    void testGetSession() {
        // Given
        String sessionId = "session-123";
        ChatSession session = ChatSession.builder()
                .sessionId(sessionId)
                .userId("user-123")
                .status(SessionStatus.ACTIVE)
                .build();

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));

        // When
        Optional<ChatSession> result = chatSessionService.getSession(sessionId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSessionId()).isEqualTo(sessionId);
    }

    @Test
    @DisplayName("Should close session")
    void testCloseSession() {
        // Given
        String sessionId = "session-to-close";
        ChatSession session = ChatSession.builder()
                .sessionId(sessionId)
                .userId("user-123")
                .status(SessionStatus.ACTIVE)
                .build();

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(ChatSession.class))).thenReturn(session);

        // When
        chatSessionService.closeSession(sessionId);

        // Then
        ArgumentCaptor<ChatSession> captor = ArgumentCaptor.forClass(ChatSession.class);
        verify(sessionRepository).save(captor.capture());
        
        ChatSession saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(SessionStatus.CLOSED);
        assertThat(saved.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should get active sessions for user")
    void testGetActiveSessions() {
        // Given
        String userId = "user-123";
        List<ChatSession> activeSessions = Arrays.asList(
                ChatSession.builder().sessionId("s1").userId(userId).status(SessionStatus.ACTIVE).build(),
                ChatSession.builder().sessionId("s2").userId(userId).status(SessionStatus.ACTIVE).build()
        );

        when(sessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE))
                .thenReturn(activeSessions);

        // When
        List<ChatSession> result = chatSessionService.getActiveSessions(userId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.getStatus() == SessionStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should get all user sessions")
    void testGetUserSessions() {
        // Given
        String userId = "user-123";
        List<ChatSession> sessions = Arrays.asList(
                ChatSession.builder().sessionId("s1").userId(userId).status(SessionStatus.ACTIVE).build(),
                ChatSession.builder().sessionId("s2").userId(userId).status(SessionStatus.CLOSED).build(),
                ChatSession.builder().sessionId("s3").userId(userId).status(SessionStatus.ARCHIVED).build()
        );

        when(sessionRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(sessions);

        // When
        List<ChatSession> result = chatSessionService.getUserSessions(userId);

        // Then
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("Should archive stale sessions")
    void testArchiveStaleSessions() {
        // Given
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<ChatSession> staleSessions = Arrays.asList(
                ChatSession.builder().sessionId("s1").userId("u1").status(SessionStatus.ACTIVE).build(),
                ChatSession.builder().sessionId("s2").userId("u2").status(SessionStatus.ACTIVE).build()
        );

        when(sessionRepository.findByUpdatedAtBeforeAndStatus(any(LocalDateTime.class), eq(SessionStatus.ACTIVE)))
                .thenReturn(staleSessions);

        // When
        chatSessionService.archiveStaleSessions();

        // Then
        verify(sessionRepository, times(2)).save(any(ChatSession.class));
    }

    @Test
    @DisplayName("Should ensure session is active")
    void testEnsureSessionActive() {
        // Given
        String sessionId = "session-123";
        String userId = "user-123";

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());
        when(sessionRepository.countActiveSessionsByUser(userId)).thenReturn(0L);
        when(sessionRepository.save(any(ChatSession.class))).thenReturn(
                ChatSession.builder().sessionId(sessionId).userId(userId).status(SessionStatus.ACTIVE).build()
        );

        // When
        chatSessionService.ensureSessionActive(sessionId, userId);

        // Then
        verify(sessionRepository).save(any(ChatSession.class));
    }

    @Test
    @DisplayName("Should reactivate closed session")
    void testEnsureSessionActiveReactivate() {
        // Given
        String sessionId = "closed-session";
        String userId = "user-123";
        ChatSession closedSession = ChatSession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .status(SessionStatus.CLOSED)
                .build();

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(closedSession));

        // When
        chatSessionService.ensureSessionActive(sessionId, userId);

        // Then
        ArgumentCaptor<ChatSession> captor = ArgumentCaptor.forClass(ChatSession.class);
        verify(sessionRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SessionStatus.ACTIVE);
    }
}
