package com.gearup.shared.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class EventModelTest {

    @Test
    void invoiceEventFields() {
        InvoiceCreatedEvent e = new InvoiceCreatedEvent();
        e.setInvoiceId("inv-123");
        e.setCustomerName("cust-1");
        e.setAmount(100.5);
        assertEquals("inv-123", e.getInvoiceId());
        assertEquals("cust-1", e.getCustomerName());
        assertEquals(Double.valueOf(100.5), e.getAmount());
    }
}
