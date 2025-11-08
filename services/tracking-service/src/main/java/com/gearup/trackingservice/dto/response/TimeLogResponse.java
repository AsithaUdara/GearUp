package com.gearup.trackingservice.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeLogResponse {
    private Long id;
    private String logId;
    private String taskId;
    private String employeeId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime stopTime;
    private Integer durationMinutes;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

