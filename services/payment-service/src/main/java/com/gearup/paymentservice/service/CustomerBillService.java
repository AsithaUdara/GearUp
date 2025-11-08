package com.gearup.paymentservice.service;

import com.gearup.paymentservice.dto.response.CustomerBillResponseDTO;
import com.gearup.paymentservice.enums.PaymentStatus;

import java.util.List;
import java.util.UUID;

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
