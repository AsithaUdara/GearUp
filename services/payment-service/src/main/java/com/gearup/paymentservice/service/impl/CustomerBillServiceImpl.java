package com.gearup.paymentservice.service.impl;

import com.gearup.paymentservice.dto.response.CustomerBillResponseDTO;
import com.gearup.paymentservice.dto.response.ServiceItemResponseDTO;
import com.gearup.paymentservice.enums.PaymentStatus;
import com.gearup.paymentservice.exception.CustomerBillNotFoundException;
import com.gearup.paymentservice.exception.InvalidStatusTransitionException;
import com.gearup.paymentservice.model.CustomerBill;
import com.gearup.paymentservice.repository.CustomerBillRepository;
import com.gearup.paymentservice.service.CustomerBillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerBillServiceImpl implements CustomerBillService {

    private final CustomerBillRepository customerBillRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CustomerBillResponseDTO> getCustomerBills(String email) {
        log.info("Fetching bills for customer: {}", email);
        return customerBillRepository.findByCustomerEmail(email).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerBillResponseDTO getBillById(UUID id) {
        log.info("Fetching bill with ID: {}", id);
        CustomerBill bill = customerBillRepository.findById(id)
                .orElseThrow(() -> new CustomerBillNotFoundException(id));
        return mapToResponseDTO(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerBillResponseDTO> getBillsByPaymentStatus(PaymentStatus status) {
        log.info("Fetching bills with payment status: {}", status);
        return customerBillRepository.findByPaymentStatus(status).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomerBillResponseDTO markBillAsPaid(UUID id) {
        log.info("Marking bill as paid with ID: {}", id);

        CustomerBill bill = customerBillRepository.findById(id)
                .orElseThrow(() -> new CustomerBillNotFoundException(id));

        if (bill.getPaymentStatus() == PaymentStatus.PAID) {
            throw new InvalidStatusTransitionException("Bill is already marked as PAID");
        }

        bill.setPaymentStatus(PaymentStatus.PAID);
        bill.setPaidDate(LocalDate.now());

        CustomerBill updated = customerBillRepository.save(bill);
        log.info("Bill marked as paid with ID: {}", id);

        return mapToResponseDTO(updated);
    }

    /**
     * Map CustomerBill entity to ResponseDTO
     */
    private CustomerBillResponseDTO mapToResponseDTO(CustomerBill bill) {
        // Get service items from associated payment request
        List<ServiceItemResponseDTO> serviceDTOs = bill.getPaymentRequest().getServiceItems().stream()
                .map(item -> ServiceItemResponseDTO.builder()
                        .description(item.getDescription())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return CustomerBillResponseDTO.builder()
                .id(bill.getId())
                .paymentRequestId(bill.getPaymentRequest().getId())
                .customerEmail(bill.getCustomerEmail())
                .customerName(bill.getCustomerName())
                .vehicleInfo(bill.getVehicleInfo())
                .totalAmount(bill.getTotalAmount())
                .taxAmount(bill.getTaxAmount())
                .finalAmount(bill.getFinalAmount())
                .approvedDate(bill.getApprovedDate())
                .paymentStatus(bill.getPaymentStatus())
                .paidDate(bill.getPaidDate())
                .reviewSubmitted(bill.getReviewSubmitted())
                .services(serviceDTOs)
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .build();
    }
}
