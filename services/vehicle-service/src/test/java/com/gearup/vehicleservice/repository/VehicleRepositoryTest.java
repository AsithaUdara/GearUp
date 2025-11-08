package com.gearup.vehicleservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.gearup.vehicleservice.config.TestRabbitMQConfig;
import com.gearup.vehicleservice.domain.Vehicle;
import com.gearup.vehicleservice.domain.VehicleStatus;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestRabbitMQConfig.class)
@EnableAutoConfiguration(exclude = {RabbitAutoConfiguration.class, RedisAutoConfiguration.class})
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("VehicleRepository Integration Tests")
class VehicleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VehicleRepository vehicleRepository;

    private Vehicle testVehicle;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testUserId = "user-firebase-uid-123";
        
        testVehicle = new Vehicle();
        testVehicle.setId(UUID.randomUUID());
        testVehicle.setUserId(testUserId);
        testVehicle.setMake("Toyota");
        testVehicle.setModel("Camry");
        testVehicle.setYear(2022);
        testVehicle.setNumberPlate("ABC1234");
        testVehicle.setPhotoURL("https://example.com/vehicle.jpg");
        testVehicle.setStatus(VehicleStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Should save and retrieve vehicle by ID")
    void saveAndFindById_Success() {
        // Act
        Vehicle saved = vehicleRepository.save(testVehicle);
        entityManager.flush();
        entityManager.clear();

        Optional<Vehicle> found = vehicleRepository.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getUserId()).isEqualTo(testUserId);
        assertThat(found.get().getMake()).isEqualTo("Toyota");
        assertThat(found.get().getModel()).isEqualTo("Camry");
        assertThat(found.get().getYear()).isEqualTo(2022);
        assertThat(found.get().getNumberPlate()).isEqualTo("ABC1234");
        assertThat(found.get().getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Should return empty optional when vehicle not found")
    void findById_WhenNotExists_ReturnsEmpty() {
        // Act
        Optional<Vehicle> found = vehicleRepository.findById(UUID.randomUUID());

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find all vehicles by user ID")
    void findByUserId_ReturnsUserVehicles() {
        // Arrange
        vehicleRepository.save(testVehicle);
        
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setId(UUID.randomUUID());
        vehicle2.setUserId(testUserId);
        vehicle2.setMake("Honda");
        vehicle2.setModel("Civic");
        vehicle2.setYear(2021);
        vehicle2.setNumberPlate("XYZ9876");
        vehicle2.setStatus(VehicleStatus.MAINTENANCE);
        vehicleRepository.save(vehicle2);
        
        // Different user's vehicle
        Vehicle vehicle3 = new Vehicle();
        vehicle3.setId(UUID.randomUUID());
        vehicle3.setUserId("different-user-456");
        vehicle3.setMake("Ford");
        vehicle3.setModel("Focus");
        vehicle3.setYear(2020);
        vehicle3.setNumberPlate("DEF5555");
        vehicle3.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle3);
        
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Vehicle> userVehicles = vehicleRepository.findByUserId(testUserId);

        // Assert
        assertThat(userVehicles).hasSize(2);
        assertThat(userVehicles).extracting(Vehicle::getUserId)
                .containsOnly(testUserId);
        assertThat(userVehicles).extracting(Vehicle::getMake)
                .containsExactlyInAnyOrder("Toyota", "Honda");
    }

    @Test
    @DisplayName("Should return empty list when user has no vehicles")
    void findByUserId_WhenNoVehicles_ReturnsEmptyList() {
        // Act
        List<Vehicle> vehicles = vehicleRepository.findByUserId("user-with-no-vehicles");

        // Assert
        assertThat(vehicles).isEmpty();
    }

    @Test
    @DisplayName("Should update vehicle successfully")
    void updateVehicle_Success() {
        // Arrange
        Vehicle saved = vehicleRepository.save(testVehicle);
        entityManager.flush();
        entityManager.clear();

        // Act
        Vehicle toUpdate = vehicleRepository.findById(saved.getId()).orElseThrow();
        toUpdate.setMake("Nissan");
        toUpdate.setModel("Altima");
        toUpdate.setYear(2023);
        toUpdate.setNumberPlate("NEW1111");
        toUpdate.setStatus(VehicleStatus.IN_SERVICE);
        
        vehicleRepository.save(toUpdate);
        entityManager.flush();
        entityManager.clear();

        Vehicle updated = vehicleRepository.findById(saved.getId()).orElseThrow();

        // Assert
        assertThat(updated.getMake()).isEqualTo("Nissan");
        assertThat(updated.getModel()).isEqualTo("Altima");
        assertThat(updated.getYear()).isEqualTo(2023);
        assertThat(updated.getNumberPlate()).isEqualTo("NEW1111");
        assertThat(updated.getStatus()).isEqualTo(VehicleStatus.IN_SERVICE);
    }

    @Test
    @DisplayName("Should delete vehicle successfully")
    void deleteVehicle_Success() {
        // Arrange
        Vehicle saved = vehicleRepository.save(testVehicle);
        UUID vehicleId = saved.getId();
        entityManager.flush();
        entityManager.clear();

        // Act
        vehicleRepository.deleteById(vehicleId);
        entityManager.flush();
        entityManager.clear();

        Optional<Vehicle> found = vehicleRepository.findById(vehicleId);

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should enforce unique number plate constraint")
    void saveVehicle_WithDuplicateNumberPlate_ThrowsException() {
        // Arrange
        vehicleRepository.save(testVehicle);
        entityManager.flush();

        Vehicle duplicatePlate = new Vehicle();
        duplicatePlate.setId(UUID.randomUUID());
        duplicatePlate.setUserId("another-user");
        duplicatePlate.setMake("Mazda");
        duplicatePlate.setModel("3");
        duplicatePlate.setYear(2021);
        duplicatePlate.setNumberPlate("ABC1234"); // Duplicate!
        duplicatePlate.setStatus(VehicleStatus.AVAILABLE);

        // Act & Assert
        assertThatThrownBy(() -> {
            vehicleRepository.save(duplicatePlate);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should count all vehicles correctly")
    void countVehicles_Success() {
        // Arrange
        vehicleRepository.save(testVehicle);
        
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setId(UUID.randomUUID());
        vehicle2.setUserId("user-2");
        vehicle2.setMake("BMW");
        vehicle2.setModel("X5");
        vehicle2.setYear(2023);
        vehicle2.setNumberPlate("BMW999");
        vehicle2.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle2);
        
        entityManager.flush();

        // Act
        long count = vehicleRepository.count();

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should verify vehicle exists by ID")
    void existsById_ReturnsTrue() {
        // Arrange
        Vehicle saved = vehicleRepository.save(testVehicle);
        entityManager.flush();

        // Act
        boolean exists = vehicleRepository.existsById(saved.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when vehicle does not exist")
    void existsById_WhenNotExists_ReturnsFalse() {
        // Act
        boolean exists = vehicleRepository.existsById(UUID.randomUUID());

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should handle different vehicle statuses")
    void saveVehicle_WithDifferentStatuses() {
        // Test AVAILABLE
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setId(UUID.randomUUID());
        vehicle1.setUserId("user-1");
        vehicle1.setMake("Tesla");
        vehicle1.setModel("Model 3");
        vehicle1.setYear(2023);
        vehicle1.setNumberPlate("TSL001");
        vehicle1.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle1);

        // Test IN_SERVICE
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setId(UUID.randomUUID());
        vehicle2.setUserId("user-2");
        vehicle2.setMake("Tesla");
        vehicle2.setModel("Model S");
        vehicle2.setYear(2023);
        vehicle2.setNumberPlate("TSL002");
        vehicle2.setStatus(VehicleStatus.IN_SERVICE);
        vehicleRepository.save(vehicle2);

        // Test MAINTENANCE
        Vehicle vehicle3 = new Vehicle();
        vehicle3.setId(UUID.randomUUID());
        vehicle3.setUserId("user-3");
        vehicle3.setMake("Tesla");
        vehicle3.setModel("Model X");
        vehicle3.setYear(2023);
        vehicle3.setNumberPlate("TSL003");
        vehicle3.setStatus(VehicleStatus.MAINTENANCE);
        vehicleRepository.save(vehicle3);

        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(vehicleRepository.findById(vehicle1.getId()).orElseThrow().getStatus())
                .isEqualTo(VehicleStatus.AVAILABLE);
        assertThat(vehicleRepository.findById(vehicle2.getId()).orElseThrow().getStatus())
                .isEqualTo(VehicleStatus.IN_SERVICE);
        assertThat(vehicleRepository.findById(vehicle3.getId()).orElseThrow().getStatus())
                .isEqualTo(VehicleStatus.MAINTENANCE);
    }

    @Test
    @DisplayName("Should automatically set timestamps on save")
    void saveVehicle_SetsTimestamps() {
        // Act
        Vehicle saved = vehicleRepository.save(testVehicle);
        entityManager.flush();

        // Assert
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should retrieve all vehicles")
    void findAll_ReturnsAllVehicles() {
        // Arrange
        vehicleRepository.save(testVehicle);
        
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setId(UUID.randomUUID());
        vehicle2.setUserId("user-999");
        vehicle2.setMake("Audi");
        vehicle2.setModel("A4");
        vehicle2.setYear(2022);
        vehicle2.setNumberPlate("AUD777");
        vehicle2.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle2);
        
        entityManager.flush();

        // Act
        var allVehicles = vehicleRepository.findAll();

        // Assert
        assertThat(allVehicles).hasSize(2);
        assertThat(allVehicles).extracting(Vehicle::getMake)
                .containsExactlyInAnyOrder("Toyota", "Audi");
    }

    @Test
    @DisplayName("Should persist all vehicle fields correctly")
    void saveVehicle_PersistsAllFields() {
        // Arrange
        testVehicle.setPhotoURL("https://example.com/special-photo.jpg");
        testVehicle.setYear(2024);

        // Act
        Vehicle saved = vehicleRepository.save(testVehicle);
        entityManager.flush();
        entityManager.clear();

        Vehicle retrieved = vehicleRepository.findById(saved.getId()).orElseThrow();

        // Assert
        assertThat(retrieved.getPhotoURL()).isEqualTo("https://example.com/special-photo.jpg");
        assertThat(retrieved.getYear()).isEqualTo(2024);
        assertThat(retrieved.getUserId()).isEqualTo(testUserId);
    }
}
