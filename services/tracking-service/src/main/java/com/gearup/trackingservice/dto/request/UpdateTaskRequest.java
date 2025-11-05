package com.gearup.trackingservice.dto.request;

import com.gearup.trackingservice.entity.WorkTask;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {
    
    @NotNull(message = "Status is required")
    private WorkTask.TaskStatus status;
    
    @Min(value = 1, message = "Progress step must be between 1 and 5")
    @Max(value = 5, message = "Progress step must be between 1 and 5")
    private Integer progressStep;
    
    private String notes;
    
    private Integer actualDuration;
}
