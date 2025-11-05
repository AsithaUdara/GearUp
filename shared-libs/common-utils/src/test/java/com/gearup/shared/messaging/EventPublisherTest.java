package com.gearup.shared.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.gearup.shared.event.InvoiceCreatedEvent;
import com.gearup.shared.event.TaskAssignedEvent;
import com.gearup.shared.event.VehicleBookingCreatedEvent;

/**
 * Unit tests for EventPublisher
 */
@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private EventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        eventPublisher = new EventPublisher(rabbitTemplate);
    }

    @Test
    @DisplayName("Should publish vehicle booking created event with correct routing key")
    void testPublishVehicleBookingCreated() {
        // Arrange
        VehicleBookingCreatedEvent event = new VehicleBookingCreatedEvent(
                UUID.randomUUID().toString(),
                "user-123",
                LocalDateTime.now(),
                "booking-1",
                "vehicle-1",
                "Toyota Camry",
                "John Doe",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3),
                200.0
        );

        // Act
        eventPublisher.publishVehicleBookingCreated(event);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.VEHICLE_BOOKING_CREATED_KEY),
                eq(event)
        );
    }

    @Test
    @DisplayName("Should publish invoice created event with correct routing key")
    void testPublishInvoiceCreated() {
        // Arrange
        InvoiceCreatedEvent event = new InvoiceCreatedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setUserId("user-456");
        event.setTimestamp(LocalDateTime.now());
        event.setInvoiceId("INV-001");

        // Act
        eventPublisher.publishInvoiceCreated(event);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.INVOICE_CREATED_KEY),
                eq(event)
        );
    }

    @Test
    @DisplayName("Should publish task assigned event with correct routing key")
    void testPublishTaskAssigned() {
        // Arrange
        TaskAssignedEvent event = new TaskAssignedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setUserId("user-789");
        event.setTimestamp(LocalDateTime.now());
        event.setTaskId("TASK-001");

        // Act
        eventPublisher.publishTaskAssigned(event);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.TASK_ASSIGNED_KEY),
                eq(event)
        );
    }

    @Test
    @DisplayName("Should throw RuntimeException when RabbitTemplate fails")
    void testPublishEventFailure() {
        // Arrange
        VehicleBookingCreatedEvent event = new VehicleBookingCreatedEvent(
                UUID.randomUUID().toString(),
                "user-123",
                LocalDateTime.now(),
                "booking-1",
                "vehicle-1",
                "Toyota Camry",
                "John Doe",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3),
                200.0
        );

        doThrow(new RuntimeException("RabbitMQ connection failed"))
                .when(rabbitTemplate)
                .convertAndSend(any(String.class), any(String.class), any(Object.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            eventPublisher.publishVehicleBookingCreated(event);
        });
    }

    @Test
    @DisplayName("Should use generic publishEvent method")
    void testGenericPublishEvent() {
        // Arrange
        InvoiceCreatedEvent event = new InvoiceCreatedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setUserId("user-999");
        event.setTimestamp(LocalDateTime.now());

        // Act
        eventPublisher.publishEvent(RabbitMQConfig.INVOICE_CREATED_KEY, event);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.INVOICE_CREATED_KEY),
                eq(event)
        );
    }
}
