package com.gearup.notificationservice.integration;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.gearup.notificationservice.entity.Notification;
import com.gearup.notificationservice.repository.NotificationRepository;
import com.gearup.shared.event.InvoiceCreatedEvent;
import com.gearup.shared.event.TaskAssignedEvent;
import com.gearup.shared.event.VehicleBookingConfirmedEvent;
import com.gearup.shared.event.VehicleBookingCreatedEvent;
import com.gearup.shared.event.VehicleMaintenanceScheduledEvent;
import com.gearup.shared.messaging.EventPublisher;

/**
 * Integration tests for event-driven notification system.
 * Tests the complete flow: Event Publishing → RabbitMQ → Listener → Database
 */
@SpringBootTest
@ActiveProfiles("test")
class EventDrivenNotificationIntegrationTest {

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private NotificationRepository notificationRepository;

    private static final String TEST_USER_ID = "test-user-123";

    @BeforeEach
    void setUp() {
        // Clean up notifications for test user
        notificationRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create notification when vehicle booking created event is published")
    void testVehicleBookingCreatedEventFlow() {
        // Arrange
        VehicleBookingCreatedEvent event = new VehicleBookingCreatedEvent(
                UUID.randomUUID().toString(),
                TEST_USER_ID,
                LocalDateTime.now(),
                "BK-001",
                "VEH-001",
                "Toyota Camry 2024",
                "John Doe",
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(5),
                250.00
        );

        // Act
        eventPublisher.publishVehicleBookingCreated(event);

        // Assert - wait for async processing
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                            TEST_USER_ID, 
                            PageRequest.of(0, 10)
                    );
                    List<Notification> notifications = page.getContent();
                    assertThat(notifications).hasSize(1);
                    
                    Notification notification = notifications.get(0);
                    assertThat(notification.getTitle()).isEqualTo("Booking Created");
                    assertThat(notification.getMessage()).contains("Toyota Camry 2024");
                    assertThat(notification.getMessage()).contains("$250.00");
                    assertThat(notification.getRelatedEntityId()).isEqualTo("BK-001");
                    assertThat(notification.getRelatedEntityType()).isEqualTo("BOOKING");
                    assertThat(notification.getIsRead()).isFalse();
                });
    }

    @Test
    @DisplayName("Should create notification when vehicle booking confirmed event is published")
    void testVehicleBookingConfirmedEventFlow() {
        // Arrange
        VehicleBookingConfirmedEvent event = new VehicleBookingConfirmedEvent(
                UUID.randomUUID().toString(),
                TEST_USER_ID,
                LocalDateTime.now(),
                "BK-002",
                "Honda Accord 2024",
                "CONF-12345",
                LocalDateTime.now().plusDays(2).withHour(10).withMinute(0),
                "Downtown Office"
        );

        // Act
        eventPublisher.publishVehicleBookingConfirmed(event);

        // Assert
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                            TEST_USER_ID, 
                            PageRequest.of(0, 10)
                    );
                    List<Notification> notifications = page.getContent();
                    assertThat(notifications).hasSize(1);
                    
                    Notification notification = notifications.get(0);
                    assertThat(notification.getTitle()).isEqualTo("Booking Confirmed");
                    assertThat(notification.getMessage()).contains("Honda Accord 2024");
                    assertThat(notification.getMessage()).contains("CONF-12345");
                    assertThat(notification.getMessage()).contains("Downtown Office");
                    assertThat(notification.getRelatedEntityId()).isEqualTo("BK-002");
                });
    }

    @Test
    @DisplayName("Should create notification when vehicle maintenance scheduled event is published")
    void testVehicleMaintenanceScheduledEventFlow() {
        // Arrange
        VehicleMaintenanceScheduledEvent event = new VehicleMaintenanceScheduledEvent(
                UUID.randomUUID().toString(),
                TEST_USER_ID,
                LocalDateTime.now(),
                "MNT-001",
                "VEH-003",
                "Tesla Model 3",
                "Oil Change",
                LocalDateTime.now().plusDays(7),
                "Express Auto Service",
                89.99
        );

        // Act
        eventPublisher.publishVehicleMaintenanceScheduled(event);

        // Assert
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                            TEST_USER_ID, 
                            PageRequest.of(0, 10)
                    );
                    List<Notification> notifications = page.getContent();
                    assertThat(notifications).hasSize(1);
                    
                    Notification notification = notifications.get(0);
                    assertThat(notification.getTitle()).isEqualTo("Maintenance Scheduled");
                    assertThat(notification.getMessage()).contains("Tesla Model 3");
                    assertThat(notification.getMessage()).contains("Oil Change");
                    assertThat(notification.getMessage()).contains("$89.99");
                    assertThat(notification.getRelatedEntityType()).isEqualTo("MAINTENANCE");
                });
    }

    @Test
    @DisplayName("Should create notification when invoice created event is published")
    void testInvoiceCreatedEventFlow() {
        // Arrange
        InvoiceCreatedEvent event = new InvoiceCreatedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setUserId(TEST_USER_ID);
        event.setTimestamp(LocalDateTime.now());
        event.setInvoiceId("INV-001");
        event.setInvoiceNumber("2024-100");
        event.setCustomerName("Jane Smith");
        event.setAmount(450.75);

        // Act
        eventPublisher.publishInvoiceCreated(event);

        // Assert
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                            TEST_USER_ID, 
                            PageRequest.of(0, 10)
                    );
                    List<Notification> notifications = page.getContent();
                    assertThat(notifications).hasSize(1);
                    
                    Notification notification = notifications.get(0);
                    assertThat(notification.getTitle()).isEqualTo("New Invoice Created");
                    assertThat(notification.getMessage()).contains("2024-100");
                    assertThat(notification.getMessage()).contains("Jane Smith");
                    assertThat(notification.getRelatedEntityId()).isEqualTo("INV-001");
                });
    }

    @Test
    @DisplayName("Should create notification when task assigned event is published")
    void testTaskAssignedEventFlow() {
        // Arrange
        TaskAssignedEvent event = new TaskAssignedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setUserId(TEST_USER_ID);
        event.setTimestamp(LocalDateTime.now());
        event.setTaskId("TSK-001");
        event.setTaskTitle("Complete Vehicle Inspection");
        event.setAssignedBy("Manager");
        event.setDueDate(LocalDateTime.now().plusDays(2).toString());
        event.setPriority("HIGH");

        // Act
        eventPublisher.publishTaskAssigned(event);

        // Assert
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                            TEST_USER_ID, 
                            PageRequest.of(0, 10)
                    );
                    List<Notification> notifications = page.getContent();
                    assertThat(notifications).hasSize(1);
                    
                    Notification notification = notifications.get(0);
                    assertThat(notification.getTitle()).isEqualTo("New Task Assigned");
                    assertThat(notification.getMessage()).contains("Complete Vehicle Inspection");
                    assertThat(notification.getMessage()).contains("Manager");
                    assertThat(notification.getRelatedEntityId()).isEqualTo("TSK-001");
                });
    }

    @Test
    @DisplayName("Should handle multiple events for same user")
    void testMultipleEventsForSameUser() {
        // Arrange & Act - Publish multiple events
        VehicleBookingCreatedEvent bookingEvent = new VehicleBookingCreatedEvent(
                UUID.randomUUID().toString(),
                TEST_USER_ID,
                LocalDateTime.now(),
                "BK-003",
                "VEH-004",
                "BMW 3 Series",
                "Test User",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3),
                300.00
        );
        
        TaskAssignedEvent taskEvent = new TaskAssignedEvent();
        taskEvent.setEventId(UUID.randomUUID().toString());
        taskEvent.setUserId(TEST_USER_ID);
        taskEvent.setTimestamp(LocalDateTime.now());
        taskEvent.setTaskId("TSK-002");
        taskEvent.setTaskTitle("Review Documents");
        taskEvent.setAssignedBy("Supervisor");
        taskEvent.setDueDate(LocalDateTime.now().plusDays(1).toString());
        taskEvent.setPriority("MEDIUM");

        eventPublisher.publishVehicleBookingCreated(bookingEvent);
        eventPublisher.publishTaskAssigned(taskEvent);

        // Assert
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                            TEST_USER_ID, 
                            PageRequest.of(0, 10)
                    );
                    List<Notification> notifications = page.getContent();
                    assertThat(notifications).hasSize(2);
                    
                    // Verify both notifications were created
                    assertThat(notifications)
                            .extracting(Notification::getTitle)
                            .containsExactlyInAnyOrder("Booking Created", "New Task Assigned");
                });
    }
}
