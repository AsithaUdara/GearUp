package com.gearup.trackingservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gearup.trackingservice.entity.PartsRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartsRequestResponse {
    
    private Long id;
    private String requestId;
    private String material;
    private Integer quantity;
    private PartsRequest.RequestStatus status;
    private LocalDate date;
    private String vehicle;
    private String serviceId;
    private String requestedBy;
    private String notes;
    private BigDecimal cost;
}
