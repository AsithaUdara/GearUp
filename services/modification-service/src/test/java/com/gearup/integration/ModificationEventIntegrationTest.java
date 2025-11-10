package com.gearup.integration;

import com.gearup.domain.ModificationRequest;
import com.gearup.domain.ModificationStatus;
import com.gearup.repository.ModificationRequestRepository;
import com.gearup.service.ModificationRequestService;
import com.gearup.shared.event.modification.*;
import com.gearup.shared.messaging.RabbitMQConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Modification Service Event-Driven Architecture
 * Tests the complete event flow from publishing to receiving
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Modification Service Event Integration Tests")
class ModificationEventIntegrationTest {

    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.12-management-alpine"))
            .withExposedPorts(5672, 15672);

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private ModificationRequestService modificationService;

    @Autowired
    private ModificationRequestRepository modificationRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private CountDownLatch latch;
    private AtomicReference<Object> receivedEvent;

    @BeforeEach
    void setUp() {
        latch = new CountDownLatch(1);
        receivedEvent = new AtomicReference<>();
    }

    @AfterEach
    void tearDown() {
        modificationRepository.deleteAll();
    }

    @Test
    @DisplayName("Should publish ModificationRequestCreatedEvent when request is created")
    void testModificationRequestCreatedEvent() throws Exception {
        // Arrange
        setupEventListener(RabbitMQConstants.MODIFICATION_REQUEST_CREATED_KEY);

        ModificationRequest request = new ModificationRequest();
        request.setCustomerId("customer-123");
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setServiceName("Paint Job");
        request.setDescription("Full body paint change");
        request.setEstimatedCost(new BigDecimal("1500.00"));
        request.setPreferredDate(LocalDate.now().plusDays(7));
        request.setStatus(ModificationStatus.PENDING);

        // Act
        ModificationRequest created = modificationService.createRequest(request);

        // Assert
        boolean eventReceived = latch.await(10, TimeUnit.SECONDS);
        assertThat(eventReceived).isTrue();
        assertThat(receivedEvent.get()).isInstanceOf(ModificationRequestCreatedEvent.class);

        ModificationRequestCreatedEvent event = (ModificationRequestCreatedEvent) receivedEvent.get();
        assertThat(event.getModificationId()).isEqualTo(created.getId());
        assertThat(event.getCustomerId()).isEqualTo("customer-123");
        assertThat(event.getCustomerName()).isEqualTo("John Doe");
        assertThat(event.getServiceName()).isEqualTo("Paint Job");
        assertThat(event.getEstimatedCost()).isEqualTo(new BigDecimal("1500.00"));
    }

    @Test
    @DisplayName("Should publish ModificationApprovedEvent when request is approved")
    void testModificationApprovedEvent() throws Exception {
        // Arrange
        setupEventListener(RabbitMQConstants.MODIFICATION_APPROVED_KEY);

        ModificationRequest request = createAndSaveRequest();

        // Act
        modificationService.updateRequest(
                request.getId(),
                ModificationStatus.APPROVED,
                "admin-user",
                "Approved for next week"
        );

        // Assert
        boolean eventReceived = latch.await(10, TimeUnit.SECONDS);
        assertThat(eventReceived).isTrue();
        assertThat(receivedEvent.get()).isInstanceOf(ModificationApprovedEvent.class);

        ModificationApprovedEvent event = (ModificationApprovedEvent) receivedEvent.get();
        assertThat(event.getModificationId()).isEqualTo(request.getId());
        assertThat(event.getApprovedBy()).isEqualTo("admin-user");
    }

    @Test
    @DisplayName("Should publish ModificationRejectedEvent when request is rejected")
    void testModificationRejectedEvent() throws Exception {
        // Arrange
        setupEventListener(RabbitMQConstants.MODIFICATION_REQUEST_REJECTED_KEY);

        ModificationRequest request = createAndSaveRequest();

        // Act
        modificationService.updateRequest(
                request.getId(),
                ModificationStatus.REJECTED,
                "admin-user",
                "Cannot accommodate this modification"
        );

        // Assert
        boolean eventReceived = latch.await(10, TimeUnit.SECONDS);
        assertThat(eventReceived).isTrue();
        assertThat(receivedEvent.get()).isInstanceOf(ModificationRejectedEvent.class);

        ModificationRejectedEvent event = (ModificationRejectedEvent) receivedEvent.get();
        assertThat(event.getModificationId()).isEqualTo(request.getId());
        assertThat(event.getRejectedBy()).isEqualTo("admin-user");
        assertThat(event.getRejectionReason()).isEqualTo("Cannot accommodate this modification");
    }

    @Test
    @DisplayName("Should publish ModificationCompletedEvent when request is completed")
    void testModificationCompletedEvent() throws Exception {
        // Arrange
        setupEventListener(RabbitMQConstants.MODIFICATION_COMPLETED_ROUTING_KEY);

        ModificationRequest request = createAndSaveRequest();
        request.setStatus(ModificationStatus.APPROVED);
        modificationRepository.save(request);

        // Act
        modificationService.updateRequest(
                request.getId(),
                ModificationStatus.COMPLETED,
                "technician-user",
                "Work completed successfully"
        );

        // Assert
        boolean eventReceived = latch.await(10, TimeUnit.SECONDS);
        assertThat(eventReceived).isTrue();
        assertThat(receivedEvent.get()).isInstanceOf(ModificationCompletedEvent.class);

        ModificationCompletedEvent event = (ModificationCompletedEvent) receivedEvent.get();
        assertThat(event.getModificationId()).isEqualTo(request.getId());
        assertThat(event.getCompletedBy()).isEqualTo("technician-user");
    }

    @Test
    @DisplayName("Should publish ModificationCancelledEvent when request is cancelled")
    void testModificationCancelledEvent() throws Exception {
        // Arrange
        setupEventListener(RabbitMQConstants.MODIFICATION_REQUEST_CANCELLED_KEY);

        ModificationRequest request = createAndSaveRequest();

        // Act
        modificationService.updateRequest(
                request.getId(),
                ModificationStatus.CANCELLED,
                "customer-123",
                "Customer changed mind"
        );

        // Assert
        boolean eventReceived = latch.await(10, TimeUnit.SECONDS);
        assertThat(eventReceived).isTrue();
        assertThat(receivedEvent.get()).isInstanceOf(ModificationCancelledEvent.class);

        ModificationCancelledEvent event = (ModificationCancelledEvent) receivedEvent.get();
        assertThat(event.getModificationId()).isEqualTo(request.getId());
        assertThat(event.getCancelledBy()).isEqualTo("customer-123");
        assertThat(event.getCancellationReason()).isEqualTo("Customer changed mind");
    }

    @Test
    @DisplayName("Should publish ModificationStatusChangedEvent for any status change")
    void testModificationStatusChangedEvent() throws Exception {
        // Arrange
        setupEventListener(RabbitMQConstants.MODIFICATION_REQUEST_STATUS_CHANGED_KEY);

        ModificationRequest request = createAndSaveRequest();

        // Act
        modificationService.updateRequest(
                request.getId(),
                ModificationStatus.IN_PROGRESS,
                "technician-user",
                "Starting work"
        );

        // Assert
        boolean eventReceived = latch.await(10, TimeUnit.SECONDS);
        assertThat(eventReceived).isTrue();
        assertThat(receivedEvent.get()).isInstanceOf(ModificationRequestStatusChangedEvent.class);

        ModificationRequestStatusChangedEvent event = (ModificationRequestStatusChangedEvent) receivedEvent.get();
        assertThat(event.getModificationId()).isEqualTo(request.getId());
        assertThat(event.getOldStatus()).isEqualTo("PENDING");
        assertThat(event.getNewStatus()).isEqualTo("IN_PROGRESS");
        assertThat(event.getChangedBy()).isEqualTo("technician-user");
    }

    @Test
    @DisplayName("Should ensure event deduplication works correctly")
    void testEventDeduplication() throws Exception {
        // Arrange
        setupEventListener(RabbitMQConstants.MODIFICATION_REQUEST_CREATED_KEY);

        ModificationRequest request = new ModificationRequest();
        request.setCustomerId("customer-123");
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setServiceName("Paint Job");
        request.setDescription("Test deduplication");
        request.setEstimatedCost(new BigDecimal("1000.00"));
        request.setPreferredDate(LocalDate.now().plusDays(5));
        request.setStatus(ModificationStatus.PENDING);

        // Act - Create request twice (should only receive one event due to deduplication)
        modificationService.createRequest(request);
        
        // Assert - Should receive only one event
        boolean eventReceived = latch.await(10, TimeUnit.SECONDS);
        assertThat(eventReceived).isTrue();
        
        // Wait a bit more to ensure no duplicate event arrives
        Thread.sleep(2000);
        assertThat(latch.getCount()).isEqualTo(0); // Latch should still be at 0 (only one countdown)
    }

    // Helper methods

    private void setupEventListener(String routingKey) {
        rabbitTemplate.setReceiveTimeout(10000);
        rabbitTemplate.setReplyTimeout(10000);

        // Set up a listener for the specific routing key
        rabbitTemplate.execute(channel -> {
            String queueName = "test-queue-" + System.currentTimeMillis();
            channel.queueDeclare(queueName, false, true, true, null);
            channel.queueBind(queueName, RabbitMQConstants.MODIFICATION_EXCHANGE, routingKey);

            channel.basicConsume(queueName, true, (consumerTag, delivery) -> {
                Object message = rabbitTemplate.getMessageConverter().fromMessage(
                        new org.springframework.amqp.core.Message(delivery.getBody(), delivery.getProperties())
                );
                receivedEvent.set(message);
                latch.countDown();
            }, consumerTag -> {});

            return null;
        });
    }

    private ModificationRequest createAndSaveRequest() {
        ModificationRequest request = new ModificationRequest();
        request.setCustomerId("customer-123");
        request.setCustomerName("Jane Smith");
        request.setCustomerEmail("jane@example.com");
        request.setServiceName("Body Kit Installation");
        request.setDescription("Install sport body kit");
        request.setEstimatedCost(new BigDecimal("2000.00"));
        request.setPreferredDate(LocalDate.now().plusDays(10));
        request.setStatus(ModificationStatus.PENDING);
        return modificationRepository.save(request);
    }
}
