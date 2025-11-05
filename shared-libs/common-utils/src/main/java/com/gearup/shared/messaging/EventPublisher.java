package com.gearup.shared.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.gearup.shared.event.BaseNotificationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Generic event publisher for all notification events.
 * Follows Single Responsibility Principle by focusing solely on event publishing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publish invoice created event
     */
    public void publishInvoiceCreated(BaseNotificationEvent event) {
        publishEvent(RabbitMQConfig.INVOICE_CREATED_KEY, event);
    }

    /**
     * Publish invoice updated event
     */
    public void publishInvoiceUpdated(BaseNotificationEvent event) {
        publishEvent(RabbitMQConfig.INVOICE_UPDATED_KEY, event);
    }

    /**
     * Publish invoice paid event
     */
    public void publishInvoicePaid(BaseNotificationEvent event) {
        publishEvent(RabbitMQConfig.INVOICE_PAID_KEY, event);
    }

    /**
     * Publish task assigned event
     */
    public void publishTaskAssigned(BaseNotificationEvent event) {
        publishEvent(RabbitMQConfig.TASK_ASSIGNED_KEY, event);
    }

    /**
     * Publish task completed event
     */
    public void publishTaskCompleted(BaseNotificationEvent event) {
        publishEvent(RabbitMQConfig.TASK_COMPLETED_KEY, event);
    }

    /**
     * Publish vehicle booking created event
     */
    public void publishVehicleBookingCreated(BaseNotificationEvent event) {
        publishEvent(RabbitMQConfig.VEHICLE_BOOKING_CREATED_KEY, event);
    }

    /**
     * Publish vehicle booking confirmed event
     */
    public void publishVehicleBookingConfirmed(BaseNotificationEvent event) {
        publishEvent(RabbitMQConfig.VEHICLE_BOOKING_CONFIRMED_KEY, event);
    }

    /**
     * Publish vehicle maintenance scheduled event
     */
    public void publishVehicleMaintenanceScheduled(BaseNotificationEvent event) {
        publishEvent(RabbitMQConfig.VEHICLE_MAINTENANCE_SCHEDULED_KEY, event);
    }

    /**
     * Generic method to publish any event with a routing key.
     * Follows DRY principle.
     */
    public void publishEvent(String routingKey, BaseNotificationEvent event) {
        try {
            log.info("Publishing event with routing key: {} - Event: {}", routingKey, event.getEventId());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    routingKey,
                    event
            );
            log.info("Successfully published event: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to publish event: {} with routing key: {}", event.getEventId(), routingKey, e);
            throw new RuntimeException("Event publishing failed", e);
        }
    }
}
