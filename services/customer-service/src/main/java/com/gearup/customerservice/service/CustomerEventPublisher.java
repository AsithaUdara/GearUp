package com.gearup.customerservice.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.gearup.shared.event.customer.*;
import com.gearup.shared.messaging.EventPublisher;
import com.gearup.shared.messaging.RabbitMQConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerEventPublisher {

    private final EventPublisher eventPublisher;

    public void publishCustomerRegisteredEvent(String customerId, String email, String displayName, String phone) {
        try {
            CustomerRegisteredEvent event = new CustomerRegisteredEvent(
                UUID.randomUUID().toString(),
                customerId,
                LocalDateTime.now(),
                email,
                displayName,
                phone
            );
            
            eventPublisher.publish(
                RabbitMQConstants.CUSTOMER_EXCHANGE,
                RabbitMQConstants.CUSTOMER_CREATED_KEY,
                event
            );
            
            log.info("Published CustomerRegisteredEvent for customer: {}", customerId);
        } catch (Exception e) {
            log.error("Failed to publish CustomerRegisteredEvent for customer: {}", customerId, e);
        }
    }

    public void publishCustomerUpdatedEvent(String customerId, String email, String displayName, String phone, String address) {
        try {
            CustomerUpdatedEvent event = new CustomerUpdatedEvent(
                UUID.randomUUID().toString(),
                customerId,
                LocalDateTime.now(),
                email,
                displayName,
                phone,
                address
            );
            
            eventPublisher.publish(
                RabbitMQConstants.CUSTOMER_EXCHANGE,
                RabbitMQConstants.CUSTOMER_UPDATED_KEY,
                event
            );
            
            log.info("Published CustomerUpdatedEvent for customer: {}", customerId);
        } catch (Exception e) {
            log.error("Failed to publish CustomerUpdatedEvent for customer: {}", customerId, e);
        }
    }

    public void publishCustomerKycChangedEvent(String customerId, String oldStatus, String newStatus, String reason) {
        try {
            CustomerKycChangedEvent event = new CustomerKycChangedEvent(
                UUID.randomUUID().toString(),
                customerId,
                LocalDateTime.now(),
                oldStatus,
                newStatus,
                reason
            );
            
            eventPublisher.publish(
                RabbitMQConstants.CUSTOMER_EXCHANGE,
                RabbitMQConstants.CUSTOMER_KYC_CHANGED_KEY,
                event
            );
            
            log.info("Published CustomerKycChangedEvent for customer: {}", customerId);
        } catch (Exception e) {
            log.error("Failed to publish CustomerKycChangedEvent for customer: {}", customerId, e);
        }
    }
}
