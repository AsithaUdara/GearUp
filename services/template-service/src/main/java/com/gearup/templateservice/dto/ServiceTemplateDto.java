package com.gearup.templateservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ServiceTemplateDto {
    private Long id;

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 2000)
    private String description;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal price;

    @NotNull
    @Min(1)
    @Max(8 * 60)
    private Integer durationMinutes; // minutes

    private Boolean active;
}
