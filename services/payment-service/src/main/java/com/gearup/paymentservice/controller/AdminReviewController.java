package com.gearup.paymentservice.controller;

import com.gearup.paymentservice.dto.response.ReviewResponseDTO;
import com.gearup.paymentservice.dto.response.ReviewStatsDTO;
import com.gearup.paymentservice.model.CustomerReview;
import com.gearup.paymentservice.service.CustomerReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminReviewController {

    private final CustomerReviewService reviewService;

    /**
     * Get all reviews
     */
    @GetMapping
    public ResponseEntity<List<ReviewResponseDTO>> getAllReviews() {
        log.info("Admin fetching all reviews");
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    /**
     * Get reviews by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByStatus(@PathVariable CustomerReview.ReviewStatus status) {
        log.info("Admin fetching reviews with status: {}", status);
        return ResponseEntity.ok(reviewService.getReviewsByStatus(status));
    }

    /**
     * Get pending reviews (waiting for admin approval)
     */
    @GetMapping("/pending")
    public ResponseEntity<List<ReviewResponseDTO>> getPendingReviews() {
        log.info("Admin fetching pending reviews");
        return ResponseEntity.ok(reviewService.getReviewsByStatus(CustomerReview.ReviewStatus.PENDING));
    }

    /**
     * Publish a review (make it visible on landing page)
     */
    @PutMapping("/{reviewId}/publish")
    public ResponseEntity<ReviewResponseDTO> publishReview(@PathVariable UUID reviewId) {
        log.info("Admin publishing review: {}", reviewId);
        return ResponseEntity.ok(reviewService.publishReview(reviewId));
    }

    /**
     * Delete a review
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID reviewId) {
        log.info("Admin deleting review: {}", reviewId);
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get review statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ReviewStatsDTO> getReviewStats() {
        log.info("Admin fetching review statistics");
        return ResponseEntity.ok(reviewService.getReviewStats());
    }

    /**
     * Get single review by ID
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> getReviewById(@PathVariable UUID reviewId) {
        log.info("Admin fetching review: {}", reviewId);
        return ResponseEntity.ok(reviewService.getReviewById(reviewId));
    }
}
