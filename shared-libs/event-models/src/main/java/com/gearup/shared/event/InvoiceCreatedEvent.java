package com.gearup.shared.event;

/**
 * Event published when an invoice is created.
 */
public class InvoiceCreatedEvent extends BaseNotificationEvent {
    private String invoiceId;
    private String invoiceNumber;
    private Double amount;
    private String customerName;

    public InvoiceCreatedEvent() {
    }

    public InvoiceCreatedEvent(String eventId, String userId, java.time.LocalDateTime timestamp,
                               String invoiceId, String invoiceNumber, Double amount, String customerName) {
        super(eventId, userId, timestamp);
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.amount = amount;
        this.customerName = customerName;
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
