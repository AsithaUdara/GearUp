package com.gearup.chatbotservice.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for chatbot responses to clients
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessageResponse {
    
    private String content;
    
    private String sessionId;
    
    private String intent;
    
    private List<String> suggestedActions;
    
    private Long timestamp;
    
    private Boolean requiresAction;
    
    private String actionType;
    
    private Object actionData;
    
    @Builder.Default
    private String sender = "bot";
    
    private LocalDateTime createdAt;
}
