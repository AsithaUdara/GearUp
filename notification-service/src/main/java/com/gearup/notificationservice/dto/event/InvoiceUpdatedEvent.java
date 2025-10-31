package com.gearup.notificationservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InvoiceUpdatedEvent extends BaseNotificationEvent {
    private String invoiceId;
    private String invoiceNumber;
    private String updateType; // e.g., "STATUS_CHANGED", "AMOUNT_UPDATED"
    private String oldValue;
    private String newValue;
}
