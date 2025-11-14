package com.gearup.dto;

import com.gearup.domain.PartsRequestStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class PartsRequestDTO {
    private UUID id;
    private String requestId;
    private String material;
    private Integer quantity;
    private String notes;
    private PartsRequestStatus status;
    private UUID createdBy;
    private String date;  // ISO date string format
    private String createdAt;  // ISO timestamp format
}