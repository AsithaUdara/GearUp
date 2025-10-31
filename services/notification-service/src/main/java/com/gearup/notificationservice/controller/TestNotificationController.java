package com.gearup.notificationservice.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gearup.notificationservice.config.RabbitMQConfig;
import com.gearup.notificationservice.dto.event.InvoiceCreatedEvent;
import com.gearup.notificationservice.dto.event.InvoicePaidEvent;
import com.gearup.notificationservice.dto.event.TaskAssignedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestNotificationController {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Test endpoint to simulate invoice created event
     */
    @PostMapping("/invoice-created")
    public ResponseEntity<String> testInvoiceCreated(Authentication authentication) {
        String userId = authentication.getName();
        
        InvoiceCreatedEvent event = InvoiceCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .invoiceId("INV-" + System.currentTimeMillis())
                .invoiceNumber("INV-2024-001")
                .amount(1500.00)
                .customerName("John Doe")
                .build();
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.INVOICE_CREATED_KEY,
                event
        );
        
        log.info("Published test InvoiceCreatedEvent for user: {}", userId);
        return ResponseEntity.ok("Invoice created event published");
    }

    /**
     * Test endpoint to simulate task assigned event
     */
    @PostMapping("/task-assigned")
    public ResponseEntity<String> testTaskAssigned(Authentication authentication) {
        String userId = authentication.getName();
        
        TaskAssignedEvent event = TaskAssignedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .taskId("TASK-" + System.currentTimeMillis())
                .taskTitle("Complete Q4 Report")
                .assignedBy("Manager")
                .dueDate("2024-12-31")
                .priority("HIGH")
                .build();
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.TASK_ASSIGNED_KEY,
                event
        );
        
        log.info("Published test TaskAssignedEvent for user: {}", userId);
        return ResponseEntity.ok("Task assigned event published");
    }

    /**
     * Test endpoint to simulate invoice paid event
     */
    @PostMapping("/invoice-paid")
    public ResponseEntity<String> testInvoicePaid(Authentication authentication) {
        String userId = authentication.getName();
        
        InvoicePaidEvent event = InvoicePaidEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .invoiceId("INV-" + System.currentTimeMillis())
                .invoiceNumber("INV-2024-002")
                .amount(2500.00)
                .paymentMethod("Credit Card")
                .build();
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.INVOICE_PAID_KEY,
                event
        );
        
        log.info("Published test InvoicePaidEvent for user: {}", userId);
        return ResponseEntity.ok("Invoice paid event published");
    }
}
