package com.gearup.paymentservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gearup.paymentservice.dto.response.ReviewResponseDTO;
import com.gearup.paymentservice.service.CustomerReviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/reviews/public")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PublicReviewController {

    private final CustomerReviewService reviewService;

    /**
     * Get published reviews for landing page (public access)
     */
    @GetMapping
    public ResponseEntity<List<ReviewResponseDTO>> getPublishedReviews() {
        log.info("Fetching published reviews for landing page");
        return ResponseEntity.ok(reviewService.getPublishedReviews());
    }
}
