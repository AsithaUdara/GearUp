package com.gearup.chatbotservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Entity representing a message in conversation history
 * Stores all user and bot interactions
 */
@Entity
@Table(name = "conversation_history", indexes = {
    @Index(name = "idx_conversation_history_session_id", columnList = "session_id"),
    @Index(name = "idx_conversation_history_user_id", columnList = "user_id"),
    @Index(name = "idx_conversation_history_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", nullable = false)
    private String sessionId;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private MessageSender sender;
    
    @Column(length = 255)
    private String intent;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Enum for message sender types
     */
    public enum MessageSender {
        USER,
        BOT,
        SYSTEM
    }
}
