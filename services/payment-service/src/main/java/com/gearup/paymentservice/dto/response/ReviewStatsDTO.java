package com.gearup.paymentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsDTO {

    private long pendingCount;
    private long publishedCount;
    private long rejectedCount;
    private double averageRating;
}
