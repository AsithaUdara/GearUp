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
public class InvoicePaidEvent extends BaseNotificationEvent {
    private String invoiceId;
    private String invoiceNumber;
    private Double amount;
    private String paymentMethod;
}
