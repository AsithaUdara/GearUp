package com.gearup.templateservice.legacy.template.web.dto;

import lombok.Builder;
import lombok.Value;
import java.time.LocalDateTime;

// Legacy placeholder DTO kept only for backward compatibility; not referenced by active code.
@Value
@Builder
public class TemplateResponse {
    Long id;
    String name;
    String description;
    Double price;
    Integer durationMinutes;
    Boolean active;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}