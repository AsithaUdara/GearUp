package com.gearup.paymentservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gearup.paymentservice.dto.response.CustomerBillResponseDTO;
import com.gearup.paymentservice.enums.PaymentStatus;
import com.gearup.paymentservice.service.CustomerBillService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/payments/customer")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Allow frontend access
public class CustomerPaymentController {

    // TODO: Add payment gateway integration (Stripe, PayPal, Square)
    // TODO: Implement payment intent creation and confirmation flow
    // TODO: Add idempotency keys for payment operations to prevent duplicates
    // TODO: Implement payment retry mechanism for failed transactions
    // TODO: Add webhook handling for async payment notifications
    // TODO: Implement fraud detection and risk scoring
    // TODO: Add support for multiple payment methods (card, bank transfer, wallet)
    // TODO: Implement refund and partial refund functionality
    // TODO: Add payment dispute handling
    // TODO: Implement payment reconciliation reports
    // TODO: Add PCI-DSS compliance measures (no card data storage)
    // TODO: Implement payment history export functionality
    // TODO: Add real-time payment status notifications via WebSocket
    // TODO: Implement payment reminders for overdue bills
    // TODO: Add support for payment plans and installments

    private final CustomerBillService customerBillService;

    /**
     * Get customer bills (with optional email or status filter)
     * GET /api/payments/customer/bills?email=jane@example.com
     * GET /api/payments/customer/bills?status=UNPAID
     */
    @GetMapping("/bills")
    public ResponseEntity<List<CustomerBillResponseDTO>> getCustomerBills(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) PaymentStatus status) {
        log.info("Fetching bills with email: {} and status: {}", email, status);

        List<CustomerBillResponseDTO> bills;
        if (email != null) {
            bills = customerBillService.getCustomerBills(email);
        } else if (status != null) {
            bills = customerBillService.getBillsByPaymentStatus(status);
        } else {
            // If no filter provided, return empty list or throw error
            throw new IllegalArgumentException("Please provide either email or status parameter");
        }

        return ResponseEntity.ok(bills);
    }

    /**
     * Get specific bill by ID
     * GET /api/payments/customer/bills/{id}
     */
    @GetMapping("/bills/{id}")
    public ResponseEntity<CustomerBillResponseDTO> getBillById(@PathVariable UUID id) {
        log.info("Fetching bill with ID: {}", id);
        CustomerBillResponseDTO response = customerBillService.getBillById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark bill as paid
     * PUT /api/payments/customer/bills/{id}/mark-paid
     */
    @PutMapping("/bills/{id}/mark-paid")
    public ResponseEntity<CustomerBillResponseDTO> markBillAsPaid(@PathVariable UUID id) {
        log.info("Received request to mark bill as paid: {}", id);
        CustomerBillResponseDTO response = customerBillService.markBillAsPaid(id);
        return ResponseEntity.ok(response);
    }
}
