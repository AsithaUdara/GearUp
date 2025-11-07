package com.gearup.analyticalservice.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopServiceDTO {
    private String serviceName;
    private Integer count;
    private BigDecimal percentage;
}
