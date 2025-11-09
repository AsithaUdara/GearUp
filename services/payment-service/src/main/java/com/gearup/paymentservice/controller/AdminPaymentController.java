package com.gearup.paymentservice.controller;

import com.gearup.paymentservice.dto.request.CreatePaymentRequestDTO;
import com.gearup.paymentservice.dto.response.PaymentRequestResponseDTO;
import com.gearup.paymentservice.dto.response.PaymentStatsDTO;
import com.gearup.paymentservice.enums.PaymentRequestStatus;
import com.gearup.paymentservice.service.PaymentRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Allow frontend access
public class AdminPaymentController {

    private final PaymentRequestService paymentRequestService;

    /**
     * Create a new payment request
     * POST /api/payments/admin/requests
     */
    @PostMapping("/requests")
    public ResponseEntity<PaymentRequestResponseDTO> createPaymentRequest(
            @Valid @RequestBody CreatePaymentRequestDTO dto) {
        log.info("Received request to create payment request for: {}", dto.getCustomerEmail());
        PaymentRequestResponseDTO response = paymentRequestService.createPaymentRequest(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all payment requests (with optional status filter)
     * GET /api/payments/admin/requests
     * GET /api/payments/admin/requests?status=PENDING
     */
    @GetMapping("/requests")
    public ResponseEntity<List<PaymentRequestResponseDTO>> getAllPaymentRequests(
            @RequestParam(required = false) PaymentRequestStatus status) {
        log.info("Fetching payment requests with status filter: {}", status);
        
        List<PaymentRequestResponseDTO> requests;
        if (status != null) {
            requests = paymentRequestService.getPaymentRequestsByStatus(status);
        } else {
            requests = paymentRequestService.getAllPaymentRequests();
        }
        
        return ResponseEntity.ok(requests);
    }

    /**
     * Get specific payment request by ID
     * GET /api/payments/admin/requests/{id}
     */
    @GetMapping("/requests/{id}")
    public ResponseEntity<PaymentRequestResponseDTO> getPaymentRequestById(@PathVariable UUID id) {
        log.info("Fetching payment request with ID: {}", id);
        PaymentRequestResponseDTO response = paymentRequestService.getPaymentRequestById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Approve a payment request
     * PUT /api/payments/admin/requests/{id}/approve
     */
    @PutMapping("/requests/{id}/approve")
    public ResponseEntity<PaymentRequestResponseDTO> approvePaymentRequest(@PathVariable UUID id) {
        log.info("Received request to approve payment request: {}", id);
        PaymentRequestResponseDTO response = paymentRequestService.approvePaymentRequest(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Reject a payment request with reason
     * PUT /api/payments/admin/requests/{id}/reject
     */
    @PutMapping("/requests/{id}/reject")
    public ResponseEntity<PaymentRequestResponseDTO> rejectPaymentRequest(
            @PathVariable UUID id,
            @RequestBody Map<String, String> payload) {
        String reason = payload.getOrDefault("reason", "No reason provided");
        log.info("Received request to reject payment request: {} with reason: {}", id, reason);
        PaymentRequestResponseDTO response = paymentRequestService.rejectPaymentRequest(id, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Get payment statistics
     * GET /api/payments/admin/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<PaymentStatsDTO> getPaymentStats() {
        log.info("Fetching payment statistics");
        PaymentStatsDTO stats = paymentRequestService.getPaymentStats();
        return ResponseEntity.ok(stats);
    }
}
