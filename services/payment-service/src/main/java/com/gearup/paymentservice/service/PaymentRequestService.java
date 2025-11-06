package com.gearup.paymentservice.service;

import com.gearup.paymentservice.dto.request.CreatePaymentRequestDTO;
import com.gearup.paymentservice.dto.response.PaymentRequestResponseDTO;
import com.gearup.paymentservice.dto.response.PaymentStatsDTO;
import com.gearup.paymentservice.enums.PaymentRequestStatus;

import java.util.List;
import java.util.UUID;

public interface PaymentRequestService {
    
    /**
     * Create a new payment request
     * @param dto Payment request creation data
     * @return Created payment request response
     */
    PaymentRequestResponseDTO createPaymentRequest(CreatePaymentRequestDTO dto);
    
    /**
     * Get all payment requests
     * @return List of all payment requests
     */
    List<PaymentRequestResponseDTO> getAllPaymentRequests();
    
    /**
     * Get payment request by ID
     * @param id Payment request ID
     * @return Payment request response
     */
    PaymentRequestResponseDTO getPaymentRequestById(UUID id);
    
    /**
     * Get payment requests by status
     * @param status Payment request status
     * @return List of payment requests with the given status
     */
    List<PaymentRequestResponseDTO> getPaymentRequestsByStatus(PaymentRequestStatus status);
    
    /**
     * Approve a payment request and automatically create a customer bill
     * @param id Payment request ID
     * @return Approved payment request response
     */
    PaymentRequestResponseDTO approvePaymentRequest(UUID id);
    
    /**
     * Reject a payment request with a reason
     * @param id Payment request ID
     * @param reason Rejection reason
     * @return Rejected payment request response
     */
    PaymentRequestResponseDTO rejectPaymentRequest(UUID id, String reason);
    
    /**
     * Get payment statistics (counts and revenue)
     * @return Payment statistics
     */
    PaymentStatsDTO getPaymentStats();
}
