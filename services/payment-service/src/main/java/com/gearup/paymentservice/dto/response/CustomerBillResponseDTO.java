package com.gearup.paymentservice.dto.response;

import com.gearup.paymentservice.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBillResponseDTO {
    private UUID id;
    private UUID paymentRequestId;
    private String customerEmail;
    private String customerName;
    private String vehicleInfo;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal finalAmount;
    private LocalDate approvedDate;
    private PaymentStatus paymentStatus;
    private LocalDate paidDate;
    private Boolean reviewSubmitted;
    private List<ServiceItemResponseDTO> services;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
