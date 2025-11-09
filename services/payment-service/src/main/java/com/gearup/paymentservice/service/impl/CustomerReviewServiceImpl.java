package com.gearup.paymentservice.service.impl;

import com.gearup.paymentservice.dto.request.ReviewSubmissionDTO;
import com.gearup.paymentservice.dto.response.ReviewResponseDTO;
import com.gearup.paymentservice.dto.response.ReviewStatsDTO;
import com.gearup.paymentservice.enums.PaymentStatus;
import com.gearup.paymentservice.exception.CustomerBillNotFoundException;
import com.gearup.paymentservice.exception.ReviewAlreadyExistsException;
import com.gearup.paymentservice.exception.ReviewNotFoundException;
import com.gearup.paymentservice.model.CustomerBill;
import com.gearup.paymentservice.model.CustomerReview;
import com.gearup.paymentservice.repository.CustomerBillRepository;
import com.gearup.paymentservice.repository.CustomerReviewRepository;
import com.gearup.paymentservice.service.CustomerReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerReviewServiceImpl implements CustomerReviewService {

    private final CustomerReviewRepository reviewRepository;
    private final CustomerBillRepository billRepository;

    @Override
    @Transactional
    public ReviewResponseDTO submitReview(ReviewSubmissionDTO reviewDTO) {
        log.info("Submitting review for bill: {}", reviewDTO.getBillId());

        // Check if bill exists and is paid
        CustomerBill bill = billRepository.findById(reviewDTO.getBillId())
                .orElseThrow(() -> new CustomerBillNotFoundException(reviewDTO.getBillId()));

        if (bill.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("Cannot submit review for unpaid bill");
        }

        // Check if review already exists for this bill
        if (reviewRepository.existsByBillId(reviewDTO.getBillId())) {
            throw new ReviewAlreadyExistsException(reviewDTO.getBillId());
        }

        // Create review
        CustomerReview review = new CustomerReview();
        review.setBillId(reviewDTO.getBillId());
        review.setCustomerEmail(reviewDTO.getCustomerEmail());
        review.setCustomerName(reviewDTO.getCustomerName());
        review.setServiceName(reviewDTO.getServiceName());
        review.setRating(reviewDTO.getRating());
        review.setReviewText(reviewDTO.getReviewText());
        review.setStatus(CustomerReview.ReviewStatus.PENDING);
        review.setSubmittedDate(LocalDateTime.now());
        review.setCreatedAt(LocalDateTime.now());

        CustomerReview savedReview = reviewRepository.save(review);

        // Update bill to mark review as submitted
        bill.setReviewSubmitted(true);
        billRepository.save(bill);

        log.info("Review submitted successfully with id: {}", savedReview.getId());
        return mapToResponseDTO(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getAllReviews() {
        log.info("Fetching all reviews");
        return reviewRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByStatus(CustomerReview.ReviewStatus status) {
        log.info("Fetching reviews with status: {}", status);
        return reviewRepository.findByStatusOrderBySubmittedDateDesc(status).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getPublishedReviews() {
        log.info("Fetching published reviews for landing page");
        return reviewRepository.findByStatusOrderBySubmittedDateDesc(CustomerReview.ReviewStatus.PUBLISHED).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getCustomerReviews(String email) {
        log.info("Fetching reviews for customer: {}", email);
        return reviewRepository.findByCustomerEmail(email).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewResponseDTO publishReview(UUID reviewId) {
        log.info("Publishing review: {}", reviewId);

        CustomerReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        review.setStatus(CustomerReview.ReviewStatus.PUBLISHED);
        review.setPublishedDate(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        CustomerReview updatedReview = reviewRepository.save(review);

        log.info("Review published successfully: {}", reviewId);
        return mapToResponseDTO(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(UUID reviewId) {
        log.info("Deleting review: {}", reviewId);

        CustomerReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        reviewRepository.delete(review);

        log.info("Review deleted successfully: {}", reviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewStatsDTO getReviewStats() {
        log.info("Fetching review statistics");

        long pendingCount = reviewRepository.countByStatus(CustomerReview.ReviewStatus.PENDING);
        long publishedCount = reviewRepository.countByStatus(CustomerReview.ReviewStatus.PUBLISHED);
        long rejectedCount = reviewRepository.countByStatus(CustomerReview.ReviewStatus.REJECTED);

        // Calculate average rating from published reviews
        List<CustomerReview> publishedReviews = reviewRepository.findByStatus(CustomerReview.ReviewStatus.PUBLISHED);
        double averageRating = publishedReviews.stream()
                .mapToInt(CustomerReview::getRating)
                .average()
                .orElse(0.0);

        return new ReviewStatsDTO(pendingCount, publishedCount, rejectedCount, averageRating);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDTO getReviewById(UUID reviewId) {
        log.info("Fetching review by id: {}", reviewId);
        CustomerReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        return mapToResponseDTO(review);
    }

    private ReviewResponseDTO mapToResponseDTO(CustomerReview review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(review.getId());
        dto.setBillId(review.getBillId());
        dto.setCustomerEmail(review.getCustomerEmail());
        dto.setCustomerName(review.getCustomerName());
        dto.setServiceName(review.getServiceName());
        dto.setRating(review.getRating());
        dto.setReviewText(review.getReviewText());
        dto.setStatus(review.getStatus().name());
        dto.setSubmittedDate(review.getSubmittedDate());
        dto.setPublishedDate(review.getPublishedDate());
        return dto;
    }
}
