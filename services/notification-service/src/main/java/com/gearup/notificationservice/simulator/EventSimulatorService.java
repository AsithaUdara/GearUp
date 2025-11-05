package com.gearup.notificationservice.simulator;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.gearup.shared.event.InvoiceCreatedEvent;
import com.gearup.shared.event.InvoicePaidEvent;
import com.gearup.shared.event.TaskAssignedEvent;
import com.gearup.shared.event.TaskCompletedEvent;
import com.gearup.shared.event.VehicleBookingConfirmedEvent;
import com.gearup.shared.event.VehicleBookingCreatedEvent;
import com.gearup.shared.event.VehicleMaintenanceScheduledEvent;
import com.gearup.shared.messaging.EventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to simulate event publishing for testing purposes.
 * This demonstrates event-driven communication patterns.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventSimulatorService {

    private final EventPublisher eventPublisher;

    /**
     * Simulate a vehicle booking creation event
     */
    public String simulateVehicleBookingCreated(String userId) {
        String eventId = UUID.randomUUID().toString();
        String bookingId = "BK-" + System.currentTimeMillis();
        
        VehicleBookingCreatedEvent event = new VehicleBookingCreatedEvent(
                eventId,
                userId,
                LocalDateTime.now(),
                bookingId,
                "VEH-001",
                "Toyota Camry 2024",
                "John Doe",
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(5),
                250.00
        );
        
        log.info("Simulating VehicleBookingCreatedEvent: {}", eventId);
        eventPublisher.publishVehicleBookingCreated(event);
        return eventId;
    }

    /**
     * Simulate a vehicle booking confirmation event
     */
    public String simulateVehicleBookingConfirmed(String userId) {
        String eventId = UUID.randomUUID().toString();
        String bookingId = "BK-" + System.currentTimeMillis();
        
        VehicleBookingConfirmedEvent event = new VehicleBookingConfirmedEvent(
                eventId,
                userId,
                LocalDateTime.now(),
                bookingId,
                "Honda Accord 2024",
                "CONF-" + System.currentTimeMillis(),
                LocalDateTime.now().plusDays(2).withHour(10).withMinute(0),
                "Downtown Office - 123 Main St"
        );
        
        log.info("Simulating VehicleBookingConfirmedEvent: {}", eventId);
        eventPublisher.publishVehicleBookingConfirmed(event);
        return eventId;
    }

    /**
     * Simulate a vehicle maintenance scheduled event
     */
    public String simulateVehicleMaintenanceScheduled(String userId) {
        String eventId = UUID.randomUUID().toString();
        String maintenanceId = "MNT-" + System.currentTimeMillis();
        
        VehicleMaintenanceScheduledEvent event = new VehicleMaintenanceScheduledEvent(
                eventId,
                userId,
                LocalDateTime.now(),
                maintenanceId,
                "VEH-002",
                "Tesla Model 3",
                "Oil Change & Tire Rotation",
                LocalDateTime.now().plusDays(7).withHour(14).withMinute(30),
                "Express Auto Service Center",
                89.99
        );
        
        log.info("Simulating VehicleMaintenanceScheduledEvent: {}", eventId);
        eventPublisher.publishVehicleMaintenanceScheduled(event);
        return eventId;
    }

    /**
     * Simulate an invoice created event
     */
    public String simulateInvoiceCreated(String userId) {
        String eventId = UUID.randomUUID().toString();
        
        InvoiceCreatedEvent event = new InvoiceCreatedEvent();
        event.setEventId(eventId);
        event.setUserId(userId);
        event.setTimestamp(LocalDateTime.now());
        event.setInvoiceId("INV-" + System.currentTimeMillis());
        event.setInvoiceNumber("2024-" + (int)(Math.random() * 1000));
        event.setCustomerName("Jane Smith");
        event.setAmount(450.75);
        
        log.info("Simulating InvoiceCreatedEvent: {}", eventId);
        eventPublisher.publishInvoiceCreated(event);
        return eventId;
    }

    /**
     * Simulate an invoice paid event
     */
    public String simulateInvoicePaid(String userId) {
        String eventId = UUID.randomUUID().toString();
        
        InvoicePaidEvent event = new InvoicePaidEvent();
        event.setEventId(eventId);
        event.setUserId(userId);
        event.setTimestamp(LocalDateTime.now());
        event.setInvoiceId("INV-" + System.currentTimeMillis());
        event.setInvoiceNumber("2024-" + (int)(Math.random() * 1000));
        event.setAmount(450.75);
        event.setPaymentMethod("Credit Card");
        
        log.info("Simulating InvoicePaidEvent: {}", eventId);
        eventPublisher.publishInvoicePaid(event);
        return eventId;
    }

    /**
     * Simulate a task assigned event
     */
    public String simulateTaskAssigned(String userId) {
        String eventId = UUID.randomUUID().toString();
        
        TaskAssignedEvent event = new TaskAssignedEvent();
        event.setEventId(eventId);
        event.setUserId(userId);
        event.setTimestamp(LocalDateTime.now());
        event.setTaskId("TSK-" + System.currentTimeMillis());
        event.setTaskTitle("Complete Vehicle Inspection");
        event.setAssignedBy("Manager");
        event.setDueDate(LocalDateTime.now().plusDays(2).toString());
        event.setPriority("HIGH");
        
        log.info("Simulating TaskAssignedEvent: {}", eventId);
        eventPublisher.publishTaskAssigned(event);
        return eventId;
    }

    /**
     * Simulate a task completed event
     */
    public String simulateTaskCompleted(String userId) {
        String eventId = UUID.randomUUID().toString();
        
        TaskCompletedEvent event = new TaskCompletedEvent();
        event.setEventId(eventId);
        event.setUserId(userId);
        event.setTimestamp(LocalDateTime.now());
        event.setTaskId("TSK-" + System.currentTimeMillis());
        event.setTaskTitle("Vehicle Delivery");
        event.setCompletedBy("John Driver");
        
        log.info("Simulating TaskCompletedEvent: {}", eventId);
        eventPublisher.publishTaskCompleted(event);
        return eventId;
    }

    /**
     * Simulate all event types for comprehensive testing
     */
    public void simulateAllEvents(String userId) {
        log.info("Starting simulation of all event types for user: {}", userId);
        
        simulateVehicleBookingCreated(userId);
        simulateVehicleBookingConfirmed(userId);
        simulateVehicleMaintenanceScheduled(userId);
        simulateInvoiceCreated(userId);
        simulateInvoicePaid(userId);
        simulateTaskAssigned(userId);
        simulateTaskCompleted(userId);
        
        log.info("Completed simulation of all event types");
    }
}
