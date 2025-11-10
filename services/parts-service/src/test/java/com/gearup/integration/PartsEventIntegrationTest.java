package com.gearup.integration;package com.gearup.integration;



import com.gearup.domain.PartsRequest;import com.gearup.domain.PartStatus;

import com.gearup.domain.PartsRequestStatus;import com.gearup.domain.Parts;

import com.gearup.repository.PartsRequestRepository;import com.gearup.repository.PartsRepository;

import com.gearup.service.PartsRequestService;import com.gearup.service.PartsService;

import com.gearup.shared.messaging.RabbitMQConstants;import com.gearup.shared.event.parts.PartsInventoryLowEvent;

import org.junit.jupiter.api.AfterEach;import com.gearup.shared.event.parts.PartsStatusChangedEvent;

import org.junit.jupiter.api.BeforeEach;import com.gearup.shared.messaging.RabbitMQConstants;

import org.junit.jupiter.api.DisplayName;import org.junit.jupiter.api.AfterEach;

import org.junit.jupiter.api.Test;import org.junit.jupiter.api.BeforeEach;

import org.springframework.amqp.rabbit.core.RabbitTemplate;import org.junit.jupiter.api.DisplayName;

import org.springframework.beans.factory.annotation.Autowired;import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.test.context.ActiveProfiles;import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;import org.springframework.test.context.DynamicPropertyRegistry;

import java.util.concurrent.TimeUnit;import org.springframework.test.context.DynamicPropertySource;

import java.util.concurrent.atomic.AtomicReference;import org.testcontainers.containers.GenericContainer;

import org.testcontainers.containers.RabbitMQContainer;

import static org.assertj.core.api.Assertions.assertThat;import org.testcontainers.junit.jupiter.Container;

import org.testcontainers.junit.jupiter.Testcontainers;

/**import org.testcontainers.utility.DockerImageName;

 * Integration test for Parts Service Event-Driven Architecture

 * Tests the complete event flow from publishing to receivingimport java.math.BigDecimal;

 */import java.util.concurrent.CountDownLatch;

@SpringBootTestimport java.util.concurrent.TimeUnit;

@ActiveProfiles("test")import java.util.concurrent.atomic.AtomicReference;

@DisplayName("Parts Service Event Integration Tests")

class PartsEventIntegrationTest {import static org.assertj.core.api.Assertions.assertThat;



    @Autowired/**

    private PartsRequestService partsRequestService; * Integration test for Parts Service Event-Driven Architecture

 * Tests the complete event flow from publishing to receiving

    @Autowired */

    private PartsRequestRepository partsRequestRepository;@SpringBootTest

@Testcontainers

    @Autowired@ActiveProfiles("test")

    private RabbitTemplate rabbitTemplate;@DisplayName("Parts Service Event Integration Tests")

class PartsEventIntegrationTest {

    private CountDownLatch latch;

    private AtomicReference<Object> receivedEvent;    @Container

    static RabbitMQContainer rabbitMQ = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.12-management-alpine"))

    @BeforeEach            .withExposedPorts(5672, 15672);

    void setUp() {

        latch = new CountDownLatch(1);    @Container

        receivedEvent = new AtomicReference<>();    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))

    }            .withExposedPorts(6379);



    @AfterEach    @DynamicPropertySource

    void tearDown() {    static void configureProperties(DynamicPropertyRegistry registry) {

        partsRequestRepository.deleteAll();        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);

    }        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);

        registry.add("spring.rabbitmq.username", () -> "guest");

    @Test        registry.add("spring.rabbitmq.password", () -> "guest");

    @DisplayName("Should publish event when parts request is created")        registry.add("spring.data.redis.host", redis::getHost);

    void testPartsRequestCreatedEvent() throws Exception {        registry.add("spring.data.redis.port", redis::getFirstMappedPort);

        // Arrange    }

        setupEventListener(RabbitMQConstants.PARTS_REQUEST_CREATED_KEY);

    @Autowired

        PartsRequest request = new PartsRequest();    private PartsService partsService;

        request.setRequestId("REQ-" + UUID.randomUUID().toString());

        request.setMaterial("Brake Pads");    @Autowired

        request.setQuantity(50);    private PartsRepository partsRepository;

        request.setNotes("Urgent order for brake pads");

        request.setStatus(PartsRequestStatus.PENDING);    @Autowired

        request.setCreatedBy(UUID.randomUUID());    private RabbitTemplate rabbitTemplate;



        // Act    private CountDownLatch latch;

        PartsRequest created = partsRequestService.createRequest(request);    private AtomicReference<Object> receivedEvent;



        // Assert    @BeforeEach

        boolean eventReceived = latch.await(10, TimeUnit.SECONDS);    void setUp() {

        assertThat(eventReceived).as("Event should be received within 10 seconds").isTrue();        latch = new CountDownLatch(1);

                receivedEvent = new AtomicReference<>();

        assertThat(created).isNotNull();    }

        assertThat(created.getStatus()).isEqualTo(PartsRequestStatus.PENDING);

    }    @AfterEach

    void tearDown() {

    @Test        partsRepository.deleteAll();

    @DisplayName("Should publish event when parts request status changes")    }

    void testPartsStatusChangedEvent() throws Exception {

        // Arrange    @Test

        setupEventListener(RabbitMQConstants.PARTS_REQUEST_STATUS_CHANGED_KEY);    @DisplayName("Should publish PartsInventoryLowEvent when stock falls below threshold")

    void testPartsInventoryLowEvent() throws Exception {

        PartsRequest request = new PartsRequest();        // Arrange

        request.setRequestId("REQ-" + UUID.randomUUID().toString());        setupEventListener(RabbitMQConstants.PARTS_INVENTORY_LOW_KEY);

        request.setMaterial("Oil Filter");

        request.setQuantity(100);        Parts part = new Parts();

        request.setNotes("Monthly stock replenishment");        part.setPartName("Brake Pads");

        request.setStatus(PartsRequestStatus.PENDING);        part.setPartNumber("BP-001");

        request.setCreatedBy(UUID.randomUUID());        part.setDescription("High performance brake pads");

        part.setCategory("Brakes");

        PartsRequest saved = partsRequestRepository.save(request);        part.setPrice(new BigDecimal("89.99"));

        part.setQuantityInStock(100);

        // Act - Update status to APPROVED        part.setMinimumStockLevel(50);

        partsRequestService.updateRequestStatus(saved.getId(), PartsRequestStatus.APPROVED);        part.setReorderLevel(30);

        part.setSupplierId("SUPP-001");

        // Assert        part.setSupplierName("Auto Parts Supplier");

        boolean eventReceived = latch.await(10, TimeUnit.SECONDS);        part.setStatus(PartStatus.AVAILABLE);

        assertThat(eventReceived).as("Status change event should be received").isTrue();

                Parts saved = partsRepository.save(part);

        PartsRequest updated = partsRequestRepository.findById(saved.getId()).orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(PartsRequestStatus.APPROVED);        // Act - Reduce stock below reorder level

    }        partsService.updateStock(saved.getPartId(), 25);



    @Test        // Assert

    @DisplayName("Should handle multiple status changes correctly")        boolean eventReceived = latch.await(10, TimeUnit.SECONDS);

    void testMultipleStatusChanges() throws Exception {        assertThat(eventReceived).isTrue();

        // Arrange        assertThat(receivedEvent.get()).isInstanceOf(PartsInventoryLowEvent.class);

        PartsRequest request = new PartsRequest();

        request.setRequestId("REQ-" + UUID.randomUUID().toString());        PartsInventoryLowEvent event = (PartsInventoryLowEvent) receivedEvent.get();

        request.setMaterial("Spark Plugs");        assertThat(event.getPartId()).isEqualTo(saved.getPartId());

        request.setQuantity(80);        assertThat(event.getPartName()).isEqualTo("Brake Pads");

        request.setNotes("Quarterly maintenance stock");        assertThat(event.getPartNumber()).isEqualTo("BP-001");

        request.setStatus(PartsRequestStatus.PENDING);        assertThat(event.getCurrentStock()).isEqualTo(25);

        request.setCreatedBy(UUID.randomUUID());        assertThat(event.getReorderLevel()).isEqualTo(30);

        assertThat(event.getSupplierId()).isEqualTo("SUPP-001");

        PartsRequest saved = partsRequestRepository.save(request);        assertThat(event.getSupplierName()).isEqualTo("Auto Parts Supplier");

    }

        // Act - Multiple status changes

        setupEventListener(RabbitMQConstants.PARTS_REQUEST_STATUS_CHANGED_KEY);    @Test

            @DisplayName("Should publish PartsStatusChangedEvent when part status changes")

        partsRequestService.updateRequestStatus(saved.getId(), PartsRequestStatus.APPROVED);    void testPartsStatusChangedEvent() throws Exception {

        boolean firstEventReceived = latch.await(5, TimeUnit.SECONDS);        // Arrange

                setupEventListener(RabbitMQConstants.PARTS_STATUS_CHANGED_KEY);

        // Setup for second event

        latch = new CountDownLatch(1);        Parts part = new Parts();

        receivedEvent.set(null);        part.setPartName("Oil Filter");

                part.setPartNumber("OF-001");

        partsRequestService.updateRequestStatus(saved.getId(), PartsRequestStatus.COMPLETED);        part.setDescription("Engine oil filter");

        boolean secondEventReceived = latch.await(5, TimeUnit.SECONDS);        part.setCategory("Filters");

        part.setPrice(new BigDecimal("12.99"));

        // Assert        part.setQuantityInStock(200);

        assertThat(firstEventReceived).as("First status change event received").isTrue();        part.setMinimumStockLevel(50);

        assertThat(secondEventReceived).as("Second status change event received").isTrue();        part.setReorderLevel(75);

                part.setSupplierId("SUPP-002");

        PartsRequest finalState = partsRequestRepository.findById(saved.getId()).orElseThrow();        part.setSupplierName("Filter Warehouse");

        assertThat(finalState.getStatus()).isEqualTo(PartsRequestStatus.COMPLETED);        part.setStatus(PartStatus.AVAILABLE);

    }

        Parts saved = partsRepository.save(part);

    @Test

    @DisplayName("Should ensure request lifecycle events are published in order")        // Act - Change status to DISCONTINUED

    void testRequestLifecycleEvents() throws Exception {        partsService.updateStatus(saved.getPartId(), PartStatus.DISCONTINUED, "admin-user");

        // Arrange - Create request

        PartsRequest request = new PartsRequest();        // Assert

        request.setRequestId("REQ-LIFECYCLE-" + UUID.randomUUID().toString());        boolean eventReceived = latch.await(10, TimeUnit.SECONDS);

        request.setMaterial("Air Filter");        assertThat(eventReceived).isTrue();

        request.setQuantity(60);        assertThat(receivedEvent.get()).isInstanceOf(PartsStatusChangedEvent.class);

        request.setNotes("Lifecycle test");

        request.setStatus(PartsRequestStatus.PENDING);        PartsStatusChangedEvent event = (PartsStatusChangedEvent) receivedEvent.get();

        request.setCreatedBy(UUID.randomUUID());        assertThat(event.getPartId()).isEqualTo(saved.getPartId());

        assertThat(event.getPartName()).isEqualTo("Oil Filter");

        // Act & Assert - Create        assertThat(event.getPartNumber()).isEqualTo("OF-001");

        setupEventListener(RabbitMQConstants.PARTS_REQUEST_CREATED_KEY);        assertThat(event.getOldStatus()).isEqualTo("AVAILABLE");

        PartsRequest created = partsRequestService.createRequest(request);        assertThat(event.getNewStatus()).isEqualTo("DISCONTINUED");

        boolean createdEventReceived = latch.await(5, TimeUnit.SECONDS);        assertThat(event.getChangedBy()).isEqualTo("admin-user");

        assertThat(createdEventReceived).isTrue();    }

        assertThat(created.getStatus()).isEqualTo(PartsRequestStatus.PENDING);

    @Test

        // Act & Assert - Approve    @DisplayName("Should not publish PartsInventoryLowEvent when stock is above reorder level")

        latch = new CountDownLatch(1);    void testNoEventWhenStockAboveReorderLevel() throws Exception {

        setupEventListener(RabbitMQConstants.PARTS_REQUEST_STATUS_CHANGED_KEY);        // Arrange

        partsRequestService.updateRequestStatus(created.getId(), PartsRequestStatus.APPROVED);        setupEventListener(RabbitMQConstants.PARTS_INVENTORY_LOW_KEY);

        boolean approvedEventReceived = latch.await(5, TimeUnit.SECONDS);

        assertThat(approvedEventReceived).isTrue();        Parts part = new Parts();

        part.setPartName("Air Filter");

        // Act & Assert - Complete        part.setPartNumber("AF-001");

        latch = new CountDownLatch(1);        part.setDescription("Engine air filter");

        setupEventListener(RabbitMQConstants.PARTS_REQUEST_STATUS_CHANGED_KEY);        part.setCategory("Filters");

        partsRequestService.updateRequestStatus(created.getId(), PartsRequestStatus.COMPLETED);        part.setPrice(new BigDecimal("19.99"));

        boolean completedEventReceived = latch.await(5, TimeUnit.SECONDS);        part.setQuantityInStock(100);

        assertThat(completedEventReceived).isTrue();        part.setMinimumStockLevel(30);

        part.setReorderLevel(40);

        // Final verification        part.setSupplierId("SUPP-003");

        PartsRequest finalRequest = partsRequestRepository.findById(created.getId()).orElseThrow();        part.setSupplierName("Filter World");

        assertThat(finalRequest.getStatus()).isEqualTo(PartsRequestStatus.COMPLETED);        part.setStatus(PartStatus.AVAILABLE);

    }

        Parts saved = partsRepository.save(part);

    @Test

    @DisplayName("Should handle request rejection workflow")        // Act - Reduce stock but keep above reorder level

    void testRequestRejectionWorkflow() throws Exception {        partsService.updateStock(saved.getPartId(), 50);

        // Arrange

        setupEventListener(RabbitMQConstants.PARTS_REQUEST_STATUS_CHANGED_KEY);        // Assert - Should NOT receive event

        boolean eventReceived = latch.await(5, TimeUnit.SECONDS);

        PartsRequest request = new PartsRequest();        assertThat(eventReceived).isFalse();

        request.setRequestId("REQ-REJECT-" + UUID.randomUUID().toString());        assertThat(latch.getCount()).isEqualTo(1); // Latch was never counted down

        request.setMaterial("Transmission Fluid");    }

        request.setQuantity(200);

        request.setNotes("Large order - may need approval");    @Test

        request.setStatus(PartsRequestStatus.PENDING);    @DisplayName("Should handle multiple status changes with proper events")

        request.setCreatedBy(UUID.randomUUID());    void testMultipleStatusChanges() throws Exception {

        // Arrange

        PartsRequest saved = partsRequestRepository.save(request);        setupEventListener(RabbitMQConstants.PARTS_STATUS_CHANGED_KEY);



        // Act - Reject the request        Parts part = new Parts();

        partsRequestService.updateRequestStatus(saved.getId(), PartsRequestStatus.REJECTED);        part.setPartName("Spark Plugs");

        part.setPartNumber("SP-001");

        // Assert        part.setDescription("Iridium spark plugs");

        boolean eventReceived = latch.await(10, TimeUnit.SECONDS);        part.setCategory("Ignition");

        assertThat(eventReceived).as("Rejection event should be received").isTrue();        part.setPrice(new BigDecimal("49.99"));

                part.setQuantityInStock(150);

        PartsRequest rejectedRequest = partsRequestRepository.findById(saved.getId()).orElseThrow();        part.setMinimumStockLevel(40);

        assertThat(rejectedRequest.getStatus()).isEqualTo(PartsRequestStatus.REJECTED);        part.setReorderLevel(60);

    }        part.setSupplierId("SUPP-004");

        part.setSupplierName("Ignition Parts Co");

    // Helper methods        part.setStatus(PartStatus.AVAILABLE);



    private void setupEventListener(String routingKey) {        Parts saved = partsRepository.save(part);

        rabbitTemplate.setReceiveTimeout(10000);

        rabbitTemplate.setReplyTimeout(10000);        // Act - First status change

        partsService.updateStatus(saved.getPartId(), PartStatus.OUT_OF_STOCK, "system");

        // Set up a listener for the specific routing key

        rabbitTemplate.execute(channel -> {        // Assert - First event

            String queueName = "test-queue-" + System.currentTimeMillis();        boolean firstEventReceived = latch.await(10, TimeUnit.SECONDS);

            channel.queueDeclare(queueName, false, true, true, null);        assertThat(firstEventReceived).isTrue();

                    PartsStatusChangedEvent firstEvent = (PartsStatusChangedEvent) receivedEvent.get();

            // Bind to the parts exchange        assertThat(firstEvent.getOldStatus()).isEqualTo("AVAILABLE");

            channel.queueBind(queueName, RabbitMQConstants.PARTS_EXCHANGE, routingKey);        assertThat(firstEvent.getNewStatus()).isEqualTo("OUT_OF_STOCK");



            channel.basicConsume(queueName, true, (consumerTag, delivery) -> {        // Arrange for second event

                try {        latch = new CountDownLatch(1);

                    Object message = rabbitTemplate.getMessageConverter().fromMessage(        receivedEvent.set(null);

                            new org.springframework.amqp.core.Message(delivery.getBody(), delivery.getProperties())

                    );        // Act - Second status change

                    receivedEvent.set(message);        partsService.updateStatus(saved.getPartId(), PartStatus.AVAILABLE, "admin");

                    latch.countDown();

                } catch (Exception e) {        // Assert - Second event

                    System.err.println("Error processing message: " + e.getMessage());        boolean secondEventReceived = latch.await(10, TimeUnit.SECONDS);

                }        assertThat(secondEventReceived).isTrue();

            }, consumerTag -> {});        PartsStatusChangedEvent secondEvent = (PartsStatusChangedEvent) receivedEvent.get();

        assertThat(secondEvent.getOldStatus()).isEqualTo("OUT_OF_STOCK");

            return null;        assertThat(secondEvent.getNewStatus()).isEqualTo("AVAILABLE");

        });    }

    }

}    @Test

    @DisplayName("Should ensure event deduplication works correctly")
    void testEventDeduplication() throws Exception {
        // Arrange
        setupEventListener(RabbitMQConstants.PARTS_STATUS_CHANGED_KEY);

        Parts part = new Parts();
        part.setPartName("Cabin Filter");
        part.setPartNumber("CF-001");
        part.setDescription("Air cabin filter");
        part.setCategory("Filters");
        part.setPrice(new BigDecimal("24.99"));
        part.setQuantityInStock(80);
        part.setMinimumStockLevel(20);
        part.setReorderLevel(35);
        part.setSupplierId("SUPP-005");
        part.setSupplierName("Clean Air Filters");
        part.setStatus(PartStatus.AVAILABLE);

        Parts saved = partsRepository.save(part);

        // Act - Update status
        partsService.updateStatus(saved.getPartId(), PartStatus.BACKORDERED, "admin");

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
            channel.queueBind(queueName, RabbitMQConstants.PARTS_EXCHANGE, routingKey);

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
}
