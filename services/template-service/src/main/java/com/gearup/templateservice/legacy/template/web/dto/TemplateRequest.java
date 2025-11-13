package com.gearup.templateservice.legacy.template.web.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

// Legacy placeholder request DTO; retained for backward compatibility only.
@Data
public class TemplateRequest {
    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 5000)
    private String description;

    @NotNull
    @PositiveOrZero
    private Double price;

    @NotNull
    @Positive
    private Integer durationMinutes;

    private Boolean active = true;
}