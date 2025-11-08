package com.gearup.paymentservice.service.impl;

import com.gearup.paymentservice.dto.request.CreatePaymentRequestDTO;
import com.gearup.paymentservice.dto.response.PaymentRequestResponseDTO;
import com.gearup.paymentservice.dto.response.PaymentStatsDTO;
import com.gearup.paymentservice.dto.response.ServiceItemResponseDTO;
import com.gearup.paymentservice.enums.PaymentRequestStatus;
import com.gearup.paymentservice.enums.PaymentStatus;
import com.gearup.paymentservice.exception.InvalidStatusTransitionException;
import com.gearup.paymentservice.exception.PaymentRequestNotFoundException;
import com.gearup.paymentservice.model.CustomerBill;
import com.gearup.paymentservice.model.PaymentRequest;
import com.gearup.paymentservice.model.ServiceItem;
import com.gearup.paymentservice.repository.CustomerBillRepository;
import com.gearup.paymentservice.repository.PaymentRequestRepository;
import com.gearup.paymentservice.service.PaymentRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestServiceImpl implements PaymentRequestService {

    private final PaymentRequestRepository paymentRequestRepository;
    private final CustomerBillRepository customerBillRepository;

    @Value("${payment.tax.rate:0.10}")
    private BigDecimal taxRate;

    @Override
    @Transactional
    public PaymentRequestResponseDTO createPaymentRequest(CreatePaymentRequestDTO dto) {
        log.info("Creating new payment request for customer: {}", dto.getCustomerEmail());

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setCustomerName(dto.getCustomerName());
        paymentRequest.setCustomerEmail(dto.getCustomerEmail());
        paymentRequest.setVehicleInfo(dto.getVehicleInfo());
        paymentRequest.setStatus(PaymentRequestStatus.PENDING);
        paymentRequest.setSubmittedBy(dto.getSubmittedBy());
        paymentRequest.setSubmittedDate(dto.getSubmittedDate() != null ? dto.getSubmittedDate() : LocalDate.now());

        // Add service items and calculate total
        BigDecimal total = BigDecimal.ZERO;
        for (var serviceDto : dto.getServices()) {
            ServiceItem item = new ServiceItem();
            item.setDescription(serviceDto.getDescription());
            item.setPrice(serviceDto.getPrice());
            paymentRequest.addServiceItem(item);
            total = total.add(serviceDto.getPrice());
        }
        paymentRequest.setTotalAmount(total);

        PaymentRequest saved = paymentRequestRepository.save(paymentRequest);
        log.info("Payment request created with ID: {}", saved.getId());

        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentRequestResponseDTO> getAllPaymentRequests() {
        log.info("Fetching all payment requests");
        return paymentRequestRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentRequestResponseDTO getPaymentRequestById(UUID id) {
        log.info("Fetching payment request with ID: {}", id);
        PaymentRequest paymentRequest = paymentRequestRepository.findById(id)
                .orElseThrow(() -> new PaymentRequestNotFoundException(id));
        return mapToResponseDTO(paymentRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentRequestResponseDTO> getPaymentRequestsByStatus(PaymentRequestStatus status) {
        log.info("Fetching payment requests with status: {}", status);
        return paymentRequestRepository.findByStatus(status).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentRequestResponseDTO approvePaymentRequest(UUID id) {
        log.info("Approving payment request with ID: {}", id);

        PaymentRequest paymentRequest = paymentRequestRepository.findById(id)
                .orElseThrow(() -> new PaymentRequestNotFoundException(id));

        if (paymentRequest.getStatus() != PaymentRequestStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    "Can only approve PENDING requests. Current status: " + paymentRequest.getStatus()
            );
        }

        // Update payment request status
        paymentRequest.setStatus(PaymentRequestStatus.APPROVED);
        paymentRequest.setApprovedDate(LocalDate.now());
        PaymentRequest approvedRequest = paymentRequestRepository.save(paymentRequest);

        // Auto-create customer bill with tax calculation
        createCustomerBill(approvedRequest);

        log.info("Payment request approved and bill created for ID: {}", id);
        return mapToResponseDTO(approvedRequest);
    }

    @Override
    @Transactional
    public PaymentRequestResponseDTO rejectPaymentRequest(UUID id, String reason) {
        log.info("Rejecting payment request with ID: {}", id);

        PaymentRequest paymentRequest = paymentRequestRepository.findById(id)
                .orElseThrow(() -> new PaymentRequestNotFoundException(id));

        if (paymentRequest.getStatus() != PaymentRequestStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    "Can only reject PENDING requests. Current status: " + paymentRequest.getStatus()
            );
        }

        paymentRequest.setStatus(PaymentRequestStatus.REJECTED);
        paymentRequest.setRejectedDate(LocalDate.now());
        paymentRequest.setRejectionReason(reason);

        PaymentRequest rejected = paymentRequestRepository.save(paymentRequest);
        log.info("Payment request rejected with ID: {}", id);

        return mapToResponseDTO(rejected);
    }

    @Override
    public PaymentStatsDTO getPaymentStats() {
        log.info("Calculating payment statistics");

        Long pendingCount = paymentRequestRepository.countByStatus(PaymentRequestStatus.PENDING);
        Long approvedCount = paymentRequestRepository.countByStatus(PaymentRequestStatus.APPROVED);
        Long rejectedCount = paymentRequestRepository.countByStatus(PaymentRequestStatus.REJECTED);

        // Calculate total revenue from approved requests
        List<PaymentRequest> approvedRequests = paymentRequestRepository.findByStatus(PaymentRequestStatus.APPROVED);
        BigDecimal totalRevenue = approvedRequests.stream()
                .map(PaymentRequest::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PaymentStatsDTO.builder()
                .pendingCount(pendingCount)
                .approvedCount(approvedCount)
                .rejectedCount(rejectedCount)
                .totalRevenue(totalRevenue)
                .build();
    }

    /**
     * Create a customer bill from an approved payment request
     */
    private void createCustomerBill(PaymentRequest paymentRequest) {
        // Calculate tax and final amount
        BigDecimal taxAmount = paymentRequest.getTotalAmount()
                .multiply(taxRate)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalAmount = paymentRequest.getTotalAmount()
                .add(taxAmount)
                .setScale(2, RoundingMode.HALF_UP);

        CustomerBill bill = new CustomerBill();
        bill.setPaymentRequest(paymentRequest);
        bill.setCustomerEmail(paymentRequest.getCustomerEmail());
        bill.setCustomerName(paymentRequest.getCustomerName());
        bill.setVehicleInfo(paymentRequest.getVehicleInfo());
        bill.setTotalAmount(paymentRequest.getTotalAmount());
        bill.setTaxAmount(taxAmount);
        bill.setFinalAmount(finalAmount);
        bill.setApprovedDate(paymentRequest.getApprovedDate());
        bill.setPaymentStatus(PaymentStatus.UNPAID);
        bill.setReviewSubmitted(false);

        customerBillRepository.save(bill);
        log.info("Customer bill created for payment request: {}", paymentRequest.getId());
    }

    /**
     * Map PaymentRequest entity to ResponseDTO
     */
    private PaymentRequestResponseDTO mapToResponseDTO(PaymentRequest paymentRequest) {
        List<ServiceItemResponseDTO> serviceDTOs = paymentRequest.getServiceItems().stream()
                .map(item -> ServiceItemResponseDTO.builder()
                        .description(item.getDescription())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return PaymentRequestResponseDTO.builder()
                .id(paymentRequest.getId())
                .customerName(paymentRequest.getCustomerName())
                .customerEmail(paymentRequest.getCustomerEmail())
                .vehicleInfo(paymentRequest.getVehicleInfo())
                .totalAmount(paymentRequest.getTotalAmount())
                .status(paymentRequest.getStatus())
                .submittedBy(paymentRequest.getSubmittedBy())
                .submittedDate(paymentRequest.getSubmittedDate())
                .approvedDate(paymentRequest.getApprovedDate())
                .rejectedDate(paymentRequest.getRejectedDate())
                .rejectionReason(paymentRequest.getRejectionReason())
                .services(serviceDTOs)
                .createdAt(paymentRequest.getCreatedAt())
                .updatedAt(paymentRequest.getUpdatedAt())
                .build();
    }
}
