package com.gearup.paymentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {

    private UUID id;
    private UUID billId;
    private String customerEmail;
    private String customerName;
    private String serviceName;
    private Integer rating;
    private String reviewText;
    private String status;
    private LocalDateTime submittedDate;
    private LocalDateTime publishedDate;
}
