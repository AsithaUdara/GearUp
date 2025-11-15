package com.gearup.shared.event.payment;

import com.gearup.shared.event.base.BasePaymentEvent;

import java.time.LocalDateTime;

/**
 * Event published when a payment is completed successfully
 */
public class PaymentCompletedEvent extends BasePaymentEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String paymentId;
    private String transactionId;
    private Double amount;
    private String paymentMethod;
    private String status;
    
    public PaymentCompletedEvent() {
        super();
    }
    
    public PaymentCompletedEvent(String eventId, String userId, LocalDateTime timestamp,
                                String paymentId, String transactionId, Double amount,
                                String paymentMethod, String status) {
        super(eventId, userId, timestamp);
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
