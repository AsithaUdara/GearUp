package com.gearup.paymentservice.dto.response;

import com.gearup.paymentservice.enums.PaymentRequestStatus;
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
public class PaymentRequestResponseDTO {
    private UUID id;
    private String customerName;
    private String customerEmail;
    private String vehicleInfo;
    private BigDecimal totalAmount;
    private PaymentRequestStatus status;
    private String submittedBy;
    private LocalDate submittedDate;
    private LocalDate approvedDate;
    private LocalDate rejectedDate;
    private String rejectionReason;
    private List<ServiceItemResponseDTO> services;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
