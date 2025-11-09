package com.gearup.analyticalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDTO {
    private LocalDateTime timestamp;
    private String event;
    private String status; // "OK", "ATTENTION", "WARNING"
    private String timeAgo; // "2m ago", "15m ago"
}
