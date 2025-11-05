package com.gearup.trackingservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.gearup.trackingservice.entity.ModificationRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModificationRequestResponse {
    
    private Long id;
    private String requestId;
    private String serviceId;
    private String vehicle;
    private String customer;
    private ModificationRequest.RequestType type;
    private String title;
    private String description;
    private ModificationRequest.RequestStatus status;
    private String requestedBy;
    private String assignedToEmployeeId;
    private BigDecimal estimatedCost;
    private Integer estimatedDuration;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime completedAt;
}
