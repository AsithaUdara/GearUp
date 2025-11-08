package com.gearup.analyticalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDashboardDTO {
    private DashboardMetricsDTO metrics;
    private List<AppointmentTrendDTO> appointmentTrend;
    private List<TopServiceDTO> topServices;
    private List<RecentActivityDTO> recentActivities;
}
