package com.gearup.paymentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatsDTO {
    private Long pendingCount;
    private Long approvedCount;
    private Long rejectedCount;
    private BigDecimal totalRevenue;
}
