package com.gearup.dto;

import com.gearup.domain.PartsRequestStatus;
import lombok.Data;

@Data
public class PartsRequestDTO {
    private String requestId;
    private String material;
    private Integer quantity;
    private String notes;
    private PartsRequestStatus status;
    private String date;  // ISO date string format
}