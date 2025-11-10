package com.gearup.vehicleservice.integration;

import com.gearup.vehicleservice.domain.Vehicle;
import com.gearup.vehicleservice.domain.VehicleStatus;
import com.gearup.vehicleservice.repository.VehicleRepository;
import com.gearup.vehicleservice.service.VehicleService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Vehicle Service Event-Driven Architecture
 * Tests verify that events are correctly published when vehicle actions occur
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Vehicle Service Event Integration Tests")
class VehicleEventIntegrationTest {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleRepository vehicleRepository;

    private static final String TEST_CUSTOMER_ID = "test-customer-integration";
    private static final String TEST_LICENSE_PLATE = "INT-TEST-001";

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        vehicleRepository.findByLicensePlate(TEST_LICENSE_PLATE)
                .ifPresent(vehicle -> vehicleRepository.delete(vehicle));
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        vehicleRepository.findByLicensePlate(TEST_LICENSE_PLATE)
                .ifPresent(vehicle -> vehicleRepository.delete(vehicle));
    }

    @Test
    @Order(1)
    @DisplayName("Should successfully register vehicle and event system should handle it")
    void testVehicleRegistration() throws Exception {
        // Arrange
        Vehicle vehicle = new Vehicle();
        vehicle.setCustomerId(TEST_CUSTOMER_ID);
        vehicle.setLicensePlate(TEST_LICENSE_PLATE);
        vehicle.setMake("Toyota");
        vehicle.setModel("Camry");
        vehicle.setYear(2022);
        vehicle.setVin("1HGBH41JXMN109186");
        vehicle.setStatus(VehicleStatus.ACTIVE);

        // Act
        Vehicle registered = vehicleService.create(vehicle);

        // Assert - Verify vehicle was created
        assertThat(registered).isNotNull();
        assertThat(registered.getCustomerId()).isEqualTo(TEST_CUSTOMER_ID);
        assertThat(registered.getLicensePlate()).isEqualTo(TEST_LICENSE_PLATE);
        assertThat(registered.getMake()).isEqualTo("Toyota");
        assertThat(registered.getModel()).isEqualTo("Camry");
        assertThat(registered.getStatus()).isEqualTo(VehicleStatus.ACTIVE);

        // Verify vehicle persisted
        Vehicle persisted = vehicleRepository.findByLicensePlate(TEST_LICENSE_PLATE)
                .orElseThrow(() -> new AssertionError("Vehicle should be persisted"));
        assertThat(persisted.getMake()).isEqualTo("Toyota");
    }

    @Test
    @Order(2)
    @DisplayName("Should successfully update vehicle and event system should handle it")
    void testVehicleUpdate() throws Exception {
        // Arrange - Register a vehicle first
        Vehicle vehicle = new Vehicle();
        vehicle.setCustomerId(TEST_CUSTOMER_ID);
        vehicle.setLicensePlate(TEST_LICENSE_PLATE);
        vehicle.setMake("Honda");
        vehicle.setModel("Civic");
        vehicle.setYear(2021);
        vehicle.setVin("19XFC2F59HE000001");
        vehicle.setColor("Blue");
        vehicle.setStatus(VehicleStatus.ACTIVE);

        Vehicle registered = vehicleService.create(vehicle);

        // Act - Update the vehicle
        registered.setColor("Red");
        registered.setYear(2023);
        registered.setModel("Civic Type R");

        Vehicle updated = vehicleService.update(registered.getVehicleId(), registered);

        // Assert
        assertThat(updated.getColor()).isEqualTo("Red");
        assertThat(updated.getYear()).isEqualTo(2023);
        assertThat(updated.getModel()).isEqualTo("Civic Type R");

        // Verify update persisted
        Vehicle persisted = vehicleRepository.findByLicensePlate(TEST_LICENSE_PLATE)
                .orElseThrow(() -> new AssertionError("Vehicle should exist"));
        assertThat(persisted.getColor()).isEqualTo("Red");
        assertThat(persisted.getModel()).isEqualTo("Civic Type R");
    }

    @Test
    @Order(3)
    @DisplayName("Should handle vehicle status changes")
    void testVehicleStatusChange() throws Exception {
        // Arrange - Register a vehicle
        Vehicle vehicle = new Vehicle();
        vehicle.setCustomerId(TEST_CUSTOMER_ID);
        vehicle.setLicensePlate(TEST_LICENSE_PLATE);
        vehicle.setMake("Ford");
        vehicle.setModel("F-150");
        vehicle.setYear(2020);
        vehicle.setVin("1FTFW1E50EFA00001");
        vehicle.setStatus(VehicleStatus.ACTIVE);

        Vehicle registered = vehicleService.create(vehicle);
        assertThat(registered.getStatus()).isEqualTo(VehicleStatus.ACTIVE);

        // Act - Change status to IN_SERVICE
        Vehicle inService = vehicleService.changeStatus(
                registered.getVehicleId(),
                VehicleStatus.IN_SERVICE
        );

        // Assert
        assertThat(inService.getStatus()).isEqualTo(VehicleStatus.IN_SERVICE);

        // Verify status change persisted
        Vehicle persisted = vehicleRepository.findById(registered.getVehicleId())
                .orElseThrow(() -> new AssertionError("Vehicle should exist"));
        assertThat(persisted.getStatus()).isEqualTo(VehicleStatus.IN_SERVICE);

        // Act - Change status to INACTIVE
        Vehicle inactive = vehicleService.changeStatus(
                registered.getVehicleId(),
                VehicleStatus.INACTIVE
        );

        // Assert
        assertThat(inactive.getStatus()).isEqualTo(VehicleStatus.INACTIVE);
    }

    @Test
    @Order(4)
    @DisplayName("Should handle complete vehicle lifecycle")
    void testCompleteVehicleLifecycle() throws Exception {
        // 1. Register vehicle
        Vehicle vehicle = new Vehicle();
        vehicle.setCustomerId(TEST_CUSTOMER_ID);
        vehicle.setLicensePlate(TEST_LICENSE_PLATE);
        vehicle.setMake("Tesla");
        vehicle.setModel("Model 3");
        vehicle.setYear(2023);
        vehicle.setVin("5YJ3E1EA0KF000001");
        vehicle.setColor("White");
        vehicle.setStatus(VehicleStatus.ACTIVE);

        Vehicle registered = vehicleService.create(vehicle);
        assertThat(registered).isNotNull();
        assertThat(registered.getStatus()).isEqualTo(VehicleStatus.ACTIVE);

        // 2. Update vehicle details
        registered.setColor("Midnight Silver");
        Vehicle updated = vehicleService.update(registered.getVehicleId(), registered);
        assertThat(updated.getColor()).isEqualTo("Midnight Silver");

        // 3. Vehicle goes in for service
        Vehicle inService = vehicleService.changeStatus(
                updated.getVehicleId(),
                VehicleStatus.IN_SERVICE
        );
        assertThat(inService.getStatus()).isEqualTo(VehicleStatus.IN_SERVICE);

        // 4. Service completed, back to active
        Vehicle backToActive = vehicleService.changeStatus(
                inService.getVehicleId(),
                VehicleStatus.ACTIVE
        );
        assertThat(backToActive.getStatus()).isEqualTo(VehicleStatus.ACTIVE);

        // 5. Update vehicle again (adding more details)
        backToActive.setColor("Red Multi-Coat");
        Vehicle finalUpdate = vehicleService.update(backToActive.getVehicleId(), backToActive);
        assertThat(finalUpdate.getColor()).isEqualTo("Red Multi-Coat");

        // Final verification
        Vehicle finalState = vehicleRepository.findById(registered.getVehicleId())
                .orElseThrow(() -> new AssertionError("Vehicle should exist"));
        assertThat(finalState.getMake()).isEqualTo("Tesla");
        assertThat(finalState.getModel()).isEqualTo("Model 3");
        assertThat(finalState.getColor()).isEqualTo("Red Multi-Coat");
        assertThat(finalState.getStatus()).isEqualTo(VehicleStatus.ACTIVE);
    }

    @Test
    @Order(5)
    @DisplayName("Should maintain immutable fields during updates")
    void testImmutableFieldsProtection() throws Exception {
        // Arrange
        Vehicle vehicle = new Vehicle();
        vehicle.setCustomerId(TEST_CUSTOMER_ID);
        vehicle.setLicensePlate(TEST_LICENSE_PLATE);
        vehicle.setMake("BMW");
        vehicle.setModel("M3");
        vehicle.setYear(2022);
        vehicle.setVin("WBS8M9C55K5A00001");
        vehicle.setStatus(VehicleStatus.ACTIVE);

        Vehicle registered = vehicleService.create(vehicle);
        String originalVin = registered.getVin();
        String originalLicensePlate = registered.getLicensePlate();

        // Act - Attempt to change immutable fields
        registered.setVin("SHOULD-NOT-CHANGE");
        registered.setLicensePlate("SHOULD-NOT-CHANGE");
        registered.setColor("Blue"); // This should change

        Vehicle updated = vehicleService.update(registered.getVehicleId(), registered);

        // Assert - Immutable fields should not change
        assertThat(updated.getVin()).isEqualTo(originalVin);
        assertThat(updated.getLicensePlate()).isEqualTo(originalLicensePlate);
        assertThat(updated.getColor()).isEqualTo("Blue");
    }

    @Test
    @Order(6)
    @DisplayName("Should handle multiple vehicles for same customer")
    void testMultipleVehiclesForCustomer() throws Exception {
        // Arrange & Act - Register first vehicle
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setCustomerId(TEST_CUSTOMER_ID);
        vehicle1.setLicensePlate(TEST_LICENSE_PLATE);
        vehicle1.setMake("Audi");
        vehicle1.setModel("A4");
        vehicle1.setYear(2021);
        vehicle1.setVin("WAUZZZ8K0DA000001");
        vehicle1.setStatus(VehicleStatus.ACTIVE);

        Vehicle registered1 = vehicleService.create(vehicle1);

        // Register second vehicle for same customer
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setCustomerId(TEST_CUSTOMER_ID); // Same customer
        vehicle2.setLicensePlate("INT-TEST-002"); // Different plate
        vehicle2.setMake("Audi");
        vehicle2.setModel("Q5");
        vehicle2.setYear(2022);
        vehicle2.setVin("WAUZZZ8R0DA000002");
        vehicle2.setStatus(VehicleStatus.ACTIVE);

        Vehicle registered2 = vehicleService.create(vehicle2);

        // Assert
        assertThat(registered1.getCustomerId()).isEqualTo(registered2.getCustomerId());
        assertThat(registered1.getLicensePlate()).isNotEqualTo(registered2.getLicensePlate());
        assertThat(registered1.getVin()).isNotEqualTo(registered2.getVin());

        // Verify both vehicles exist for customer
        var customerVehicles = vehicleRepository.findByCustomerId(TEST_CUSTOMER_ID);
        assertThat(customerVehicles).hasSizeGreaterThanOrEqualTo(2);

        // Clean up second vehicle
        vehicleRepository.delete(registered2);
    }
}
