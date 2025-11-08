package com.gearup.chatbotservice.dto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Unit tests for ChatMessageRequest DTO validation
 */
@DisplayName("ChatMessageRequest Validation Tests")
class ChatMessageRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid message request should pass validation")
    void whenAllFieldsValid_thenNoViolations() {
        // Given
        ChatMessageRequest request = ChatMessageRequest.builder()
                .content("Test message")
                .sessionId("session123")
                .userId("user123")
                .build();

        // When
        Set<ConstraintViolation<ChatMessageRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Null content should fail validation")
    void whenContentIsNull_thenViolation() {
        // Given
        ChatMessageRequest request = ChatMessageRequest.builder()
                .content(null)
                .sessionId("session123")
                .userId("user123")
                .build();

        // When
        Set<ConstraintViolation<ChatMessageRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("content"));
    }

    @Test
    @DisplayName("Empty content should fail validation")
    void whenContentIsEmpty_thenViolation() {
        // Given
        ChatMessageRequest request = ChatMessageRequest.builder()
                .content("")
                .sessionId("session123")
                .userId("user123")
                .build();

        // When
        Set<ConstraintViolation<ChatMessageRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Blank content should fail validation")
    void whenContentIsBlank_thenViolation() {
        // Given
        ChatMessageRequest request = ChatMessageRequest.builder()
                .content("   ")
                .sessionId("session123")
                .userId("user123")
                .build();

        // When
        Set<ConstraintViolation<ChatMessageRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Null sessionId should fail validation")
    void whenSessionIdIsNull_thenViolation() {
        // Given
        ChatMessageRequest request = ChatMessageRequest.builder()
                .content("Test message")
                .sessionId(null)
                .userId("user123")
                .build();

        // When
        Set<ConstraintViolation<ChatMessageRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("sessionId"));
    }

    @Test
    @DisplayName("Valid message with long content should pass")
    void whenContentIsLong_thenNoViolation() {
        // Given
        ChatMessageRequest request = ChatMessageRequest.builder()
                .content("a".repeat(5000))
                .sessionId("session123")
                .userId("user123")
                .build();

        // When
        Set<ConstraintViolation<ChatMessageRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Valid message without userId should pass (userId is optional)")
    void whenUserIdIsNull_thenNoViolation() {
        // Given - userId is optional
        ChatMessageRequest request = ChatMessageRequest.builder()
                .content("Test message")
                .sessionId("session123")
                .userId(null)
                .build();

        // When
        Set<ConstraintViolation<ChatMessageRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }
}
