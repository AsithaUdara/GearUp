package com.gearup.analyticalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceAnalyticsRequest {
    private String serviceName;
    private String serviceType;
    private Integer appointmentCount;
    private Double totalRevenue;
    private Double averageRating;
}
