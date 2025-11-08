package com.gearup.paymentservice.repository;

import com.gearup.paymentservice.enums.PaymentRequestStatus;
import com.gearup.paymentservice.model.PaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, UUID> {
    
    /**
     * Find all payment requests by status
     * @param status The payment request status (PENDING, APPROVED, REJECTED)
     * @return List of payment requests with the given status
     */
    List<PaymentRequest> findByStatus(PaymentRequestStatus status);
    
    /**
     * Find all payment requests by customer email
     * @param customerEmail The customer's email address
     * @return List of payment requests for the customer
     */
    List<PaymentRequest> findByCustomerEmail(String customerEmail);
    
    /**
     * Count payment requests by status
     * @param status The payment request status
     * @return Count of payment requests with the given status
     */
    Long countByStatus(PaymentRequestStatus status);
}
