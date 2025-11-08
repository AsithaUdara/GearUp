package com.gearup.analyticalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentTrendDTO {
    private LocalDate date;
    private Integer count;
    private String period; // "Week 1", "Week 2", etc.
}
