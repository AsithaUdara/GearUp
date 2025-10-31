package com.gearup.shared.event;

/**
 * Event published when an invoice is paid.
 */
public class InvoicePaidEvent extends BaseNotificationEvent {
    private String invoiceId;
    private String invoiceNumber;
    private Double amount;
    private String paymentMethod;

    public InvoicePaidEvent() {
    }

    public InvoicePaidEvent(String eventId, String userId, java.time.LocalDateTime timestamp,
                            String invoiceId, String invoiceNumber, Double amount, String paymentMethod) {
        super(eventId, userId, timestamp);
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
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
}
