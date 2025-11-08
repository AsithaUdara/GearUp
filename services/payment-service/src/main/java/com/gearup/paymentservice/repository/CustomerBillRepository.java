package com.gearup.paymentservice.repository;

import com.gearup.paymentservice.enums.PaymentStatus;
import com.gearup.paymentservice.model.CustomerBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerBillRepository extends JpaRepository<CustomerBill, UUID> {
    
    /**
     * Find all bills by customer email
     * @param customerEmail The customer's email address
     * @return List of bills for the customer
     */
    List<CustomerBill> findByCustomerEmail(String customerEmail);
    
    /**
     * Find all bills by payment status
     * @param paymentStatus The payment status (PAID, UNPAID)
     * @return List of bills with the given payment status
     */
    List<CustomerBill> findByPaymentStatus(PaymentStatus paymentStatus);
    
    /**
     * Find bill by payment request ID
     * @param paymentRequestId The payment request ID
     * @return Optional containing the bill if found
     */
    Optional<CustomerBill> findByPaymentRequestId(UUID paymentRequestId);
}
