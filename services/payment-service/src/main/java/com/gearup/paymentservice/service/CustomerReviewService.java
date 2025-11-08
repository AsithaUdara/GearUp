package com.gearup.paymentservice.service;

import com.gearup.paymentservice.dto.request.ReviewSubmissionDTO;
import com.gearup.paymentservice.dto.response.ReviewResponseDTO;
import com.gearup.paymentservice.dto.response.ReviewStatsDTO;
import com.gearup.paymentservice.model.CustomerReview;

import java.util.List;
import java.util.UUID;

public interface CustomerReviewService {

    /**
     * Customer submits a review after bill is paid
     */
    ReviewResponseDTO submitReview(ReviewSubmissionDTO reviewDTO);

    /**
     * Get all reviews (for admin)
     */
    List<ReviewResponseDTO> getAllReviews();

    /**
     * Get reviews by status (for admin)
     */
    List<ReviewResponseDTO> getReviewsByStatus(CustomerReview.ReviewStatus status);

    /**
     * Get published reviews (for landing page)
     */
    List<ReviewResponseDTO> getPublishedReviews();

    /**
     * Get customer's own reviews
     */
    List<ReviewResponseDTO> getCustomerReviews(String email);

    /**
     * Admin publishes a review (makes it visible on landing page)
     */
    ReviewResponseDTO publishReview(UUID reviewId);

    /**
     * Admin rejects/deletes a review
     */
    void deleteReview(UUID reviewId);

    /**
     * Get review statistics (for admin dashboard)
     */
    ReviewStatsDTO getReviewStats();

    /**
     * Get a single review by ID
     */
    ReviewResponseDTO getReviewById(UUID reviewId);
}
