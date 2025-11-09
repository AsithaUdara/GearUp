package com.gearup.chatbotservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for incoming chat messages from clients
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessageRequest {
    
    @NotBlank(message = "Message content cannot be blank")
    private String content;
    
    @NotBlank(message = "Session ID is required")
    private String sessionId;
    
    private String userId;
    
    private String context;
}
