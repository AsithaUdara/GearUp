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
import com.gearup.shared.event.InvoiceCreatedEvent;
import com.gearup.shared.event.InvoicePaidEvent;
import com.gearup.shared.event.TaskAssignedEvent;

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
        
        InvoiceCreatedEvent event = new InvoiceCreatedEvent(
                UUID.randomUUID().toString(),
                userId,
                LocalDateTime.now(),
                "INV-" + System.currentTimeMillis(),
                "INV-2024-001",
                1500.00,
                "John Doe"
        );
        
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
        
        // Construct shared TaskAssignedEvent directly (canonical event lives in shared module)
        TaskAssignedEvent event = new TaskAssignedEvent(
                UUID.randomUUID().toString(),
                userId,
                LocalDateTime.now(),
                "TASK-" + System.currentTimeMillis(),
                "Complete Q4 Report",
                "Manager",
                "2024-12-31",
                "HIGH"
        );
        
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
        
        InvoicePaidEvent event = new InvoicePaidEvent(
                UUID.randomUUID().toString(),
                userId,
                LocalDateTime.now(),
                "INV-" + System.currentTimeMillis(),
                "INV-2024-002",
                2500.00,
                "Credit Card"
        );
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.INVOICE_PAID_KEY,
                event
        );
        
        log.info("Published test InvoicePaidEvent for user: {}", userId);
        return ResponseEntity.ok("Invoice paid event published");
    }
}
