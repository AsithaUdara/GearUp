package com.gearup.analyticalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsDTO {
    private Integer appointments;
    private String appointmentsChange;
    private Integer newCustomers;
    private String newCustomersChange;
    private String growth;
}
