package com.gearup.paymentservice.repository;

import com.gearup.paymentservice.model.CustomerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerReviewRepository extends JpaRepository<CustomerReview, UUID> {

    List<CustomerReview> findByStatus(CustomerReview.ReviewStatus status);

    List<CustomerReview> findByCustomerEmail(String customerEmail);

    List<CustomerReview> findByBillId(UUID billId);

    List<CustomerReview> findByStatusOrderBySubmittedDateDesc(CustomerReview.ReviewStatus status);

    long countByStatus(CustomerReview.ReviewStatus status);

    boolean existsByBillId(UUID billId);
}
