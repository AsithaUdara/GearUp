package com.gearup.paymentservice.service;

import java.util.List;
import java.util.UUID;

import com.gearup.paymentservice.dto.response.CustomerBillResponseDTO;
import com.gearup.paymentservice.enums.PaymentStatus;

/**
 * Service interface for customer bill operations
 * 
 * TODO: Add @Transactional annotations with proper isolation levels
 * TODO: Implement optimistic locking to prevent concurrent payment updates
 * TODO: Add event sourcing for payment state changes
 * TODO: Implement comprehensive audit trail for all bill modifications
 * TODO: Add idempotency checks for payment operations
 * TODO: Implement saga pattern for distributed transactions
 * TODO: Add bill generation from appointment completion events
 * TODO: Implement automatic late payment fee calculation
 * TODO: Add payment reminder scheduling
 * TODO: Implement bill splitting functionality for shared services
 * TODO: Add support for discounts and promotional codes
 * TODO: Implement payment analytics and reporting
 */
public interface CustomerBillService {
    
    /**
     * Get all bills for a customer by email
     * @param email Customer email
     * @return List of customer bills
     */
    List<CustomerBillResponseDTO> getCustomerBills(String email);
    
    /**
     * Get bill by ID
     * @param id Bill ID
     * @return Customer bill response
     */
    CustomerBillResponseDTO getBillById(UUID id);
    
    /**
     * Get bills by payment status
     * @param status Payment status (PAID, UNPAID)
     * @return List of bills with the given status
     */
    List<CustomerBillResponseDTO> getBillsByPaymentStatus(PaymentStatus status);
    
    /**
     * Mark a bill as paid
     * @param id Bill ID
     * @return Updated customer bill response
     */
    CustomerBillResponseDTO markBillAsPaid(UUID id);
}
