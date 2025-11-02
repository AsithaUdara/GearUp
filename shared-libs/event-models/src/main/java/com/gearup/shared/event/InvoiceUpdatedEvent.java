package com.gearup.shared.event;

/**
 * Event published when an invoice is updated.
 */
public class InvoiceUpdatedEvent extends BaseNotificationEvent {
    private String invoiceId;
    private String invoiceNumber;
    private String updateType; // e.g., "STATUS_CHANGED", "AMOUNT_UPDATED"
    private String oldValue;
    private String newValue;

    public InvoiceUpdatedEvent() {
    }

    public InvoiceUpdatedEvent(String eventId, String userId, java.time.LocalDateTime timestamp,
                               String invoiceId, String invoiceNumber, String updateType, String oldValue, String newValue) {
        super(eventId, userId, timestamp);
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.updateType = updateType;
        this.oldValue = oldValue;
        this.newValue = newValue;
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

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }
}
