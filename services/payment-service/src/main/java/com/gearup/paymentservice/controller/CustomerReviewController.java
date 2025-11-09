package com.gearup.paymentservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gearup.paymentservice.dto.request.ReviewSubmissionDTO;
import com.gearup.paymentservice.dto.response.ReviewResponseDTO;
import com.gearup.paymentservice.service.CustomerReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/reviews/customer")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CustomerReviewController {

    private final CustomerReviewService reviewService;

    /**
     * Customer submits a review after payment
     */
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> submitReview(@Valid @RequestBody ReviewSubmissionDTO reviewDTO) {
        log.info("Customer submitting review for bill: {}", reviewDTO.getBillId());
        ReviewResponseDTO response = reviewService.submitReview(reviewDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get customer's own reviews
     */
    @GetMapping
    public ResponseEntity<List<ReviewResponseDTO>> getMyReviews(@RequestParam String email) {
        log.info("Customer fetching their reviews: {}", email);
        return ResponseEntity.ok(reviewService.getCustomerReviews(email));
    }
}
