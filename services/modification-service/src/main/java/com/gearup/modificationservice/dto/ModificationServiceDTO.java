package com.gearup.modificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModificationServiceDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private Integer estimatedDurationHours;
    private Boolean active;
}