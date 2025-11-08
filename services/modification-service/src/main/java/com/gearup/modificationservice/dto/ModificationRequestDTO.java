package com.gearup.modificationservice.dto;

import com.gearup.modificationservice.entity.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModificationRequestDTO {
    private Long id;
    private Long serviceId;
    private String serviceName;
    private Long customerId;
    private String customerName;
    private RequestStatus status;
    private LocalDateTime requestDate;
    private LocalDate preferredDate;
    private String notes;
    private String adminNotes;
    private BigDecimal estimatedCost;
    private BigDecimal finalCost;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime completedAt;
}