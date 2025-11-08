package com.gearup.analyticalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecentActivityRequest {
    private String eventDescription;
    private String status; // OK, ATTENTION, WARNING
    private String relatedEntityType;
    private String relatedEntityId;
}
