package com.gearup.modificationservice.dto;

import com.gearup.modificationservice.entity.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateModificationRequestDTO {
    private RequestStatus status;
    private String notes;
    private String adminNotes;
    private BigDecimal estimatedCost;
    private BigDecimal finalCost;
}