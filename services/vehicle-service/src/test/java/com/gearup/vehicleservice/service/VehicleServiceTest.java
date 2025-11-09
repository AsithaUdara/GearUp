package com.gearup.vehicleservice.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.gearup.vehicleservice.domain.Vehicle;
import com.gearup.vehicleservice.domain.VehicleStatus;
import com.gearup.vehicleservice.repository.VehicleRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleService Unit Tests")
class VehicleServiceTest {

    @Mock
    private VehicleRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle testVehicle;
    private UUID testVehicleId;

    @BeforeEach
    void setUp() {
        testVehicleId = UUID.randomUUID();
        testVehicle = new Vehicle();
        testVehicle.setId(testVehicleId);
        testVehicle.setUserId("user-firebase-uid-123");
        testVehicle.setMake("Toyota");
        testVehicle.setModel("Camry");
        testVehicle.setYear(2022);
        testVehicle.setNumberPlate("ABC1234");
        testVehicle.setPhotoURL("https://example.com/vehicle.jpg");
        testVehicle.setStatus(VehicleStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Should retrieve vehicles by user ID successfully")
    void getByUser_WithExistingVehicles_ReturnsVehicleList() {
        // Arrange
        List<Vehicle> expectedVehicles = Arrays.asList(testVehicle);
        when(repository.findByUserId("user-firebase-uid-123"))
                .thenReturn(expectedVehicles);

        // Act
        List<Vehicle> result = vehicleService.getByUser("user-firebase-uid-123");

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testVehicle);
        verify(repository, times(1)).findByUserId("user-firebase-uid-123");
    }

    @Test
    @DisplayName("Should return empty list when user has no vehicles")
    void getByUser_WithNoVehicles_ReturnsEmptyList() {
        // Arrange
        when(repository.findByUserId("user-with-no-vehicles"))
                .thenReturn(Collections.emptyList());

        // Act
        List<Vehicle> result = vehicleService.getByUser("user-with-no-vehicles");

        // Assert
        assertThat(result).isEmpty();
        verify(repository, times(1)).findByUserId("user-with-no-vehicles");
    }

    @Test
    @DisplayName("Should retrieve vehicle by ID successfully")
    void getById_WhenVehicleExists_ReturnsVehicle() {
        // Arrange
        when(repository.findById(testVehicleId))
                .thenReturn(Optional.of(testVehicle));

        // Act
        Optional<Vehicle> result = vehicleService.getById(testVehicleId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testVehicle);
        verify(repository, times(1)).findById(testVehicleId);
    }

    @Test
    @DisplayName("Should return empty optional when vehicle does not exist")
    void getById_WhenVehicleDoesNotExist_ReturnsEmpty() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act
        Optional<Vehicle> result = vehicleService.getById(nonExistentId);

        // Assert
        assertThat(result).isEmpty();
        verify(repository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should create vehicle and publish creation event")
    void create_WithValidVehicle_SavesAndPublishesEvent() {
        // Arrange
        when(repository.save(any(Vehicle.class))).thenReturn(testVehicle);

        // Act
        Vehicle result = vehicleService.create(testVehicle);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testVehicleId);
        verify(repository, times(1)).save(testVehicle);
        
        // Verify event published
        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        
        verify(rabbitTemplate, times(1)).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                payloadCaptor.capture()
        );
        
        assertThat(exchangeCaptor.getValue()).isEqualTo("vehicle.exchange");
        assertThat(routingKeyCaptor.getValue()).isEqualTo("vehicle.created");
        assertThat(payloadCaptor.getValue())
                .containsKeys("eventId", "vehicleId", "userId", "make", "model", "year", "numberPlate", "timestamp");
    }

    @Test
    @DisplayName("Should update vehicle successfully and publish update event")
    void update_WithExistingVehicle_UpdatesAndPublishesEvent() {
        // Arrange
        Vehicle updatedData = new Vehicle();
        updatedData.setMake("Honda");
        updatedData.setModel("Accord");
        updatedData.setYear(2023);
        updatedData.setNumberPlate("XYZ9876");
        updatedData.setPhotoURL("https://example.com/new-vehicle.jpg");

        when(repository.findById(testVehicleId))
                .thenReturn(Optional.of(testVehicle));
        when(repository.save(any(Vehicle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Vehicle result = vehicleService.update(testVehicleId, updatedData);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getMake()).isEqualTo("Honda");
        assertThat(result.getModel()).isEqualTo("Accord");
        assertThat(result.getYear()).isEqualTo(2023);
        assertThat(result.getNumberPlate()).isEqualTo("XYZ9876");
        
        verify(repository, times(1)).findById(testVehicleId);
        verify(repository, times(1)).save(testVehicle);
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("vehicle.exchange"),
                eq("vehicle.updated"),
                any(Map.class)
        );
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent vehicle")
    void update_WithNonExistentVehicle_ThrowsException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        Vehicle updatedData = new Vehicle();
        when(repository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> vehicleService.update(nonExistentId, updatedData))
                .isInstanceOf(NoSuchElementException.class);
        
        verify(repository, times(1)).findById(nonExistentId);
        verify(repository, never()).save(any(Vehicle.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyMap());
    }

    @Test
    @DisplayName("Should change vehicle status and publish status change event")
    void changeStatus_WithValidStatus_UpdatesAndPublishesEvent() {
        // Arrange
        testVehicle.setStatus(VehicleStatus.AVAILABLE);
        when(repository.findById(testVehicleId))
                .thenReturn(Optional.of(testVehicle));
        when(repository.save(any(Vehicle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Vehicle result = vehicleService.changeStatus(testVehicleId, VehicleStatus.IN_SERVICE);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(VehicleStatus.IN_SERVICE);
        
        verify(repository, times(1)).findById(testVehicleId);
        verify(repository, times(1)).save(testVehicle);
        
        // Verify status change event
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("vehicle.exchange"),
                routingKeyCaptor.capture(),
                payloadCaptor.capture()
        );
        
        assertThat(routingKeyCaptor.getValue()).isEqualTo("vehicle.status.changed");
        Map<String, Object> payload = payloadCaptor.getValue();
        assertThat(payload)
                .containsEntry("oldStatus", "AVAILABLE")
                .containsEntry("newStatus", "IN_SERVICE");
    }

    @Test
    @DisplayName("Should delete vehicle successfully")
    void delete_WithExistingVehicle_DeletesVehicle() {
        // Arrange
        doNothing().when(repository).deleteById(testVehicleId);

        // Act
        vehicleService.delete(testVehicleId);

        // Assert
        verify(repository, times(1)).deleteById(testVehicleId);
    }

    @Test
    @DisplayName("Should handle RabbitMQ failure gracefully without affecting operation")
    void create_WhenRabbitMQFails_StillSavesVehicle() {
        // Arrange
        when(repository.save(any(Vehicle.class))).thenReturn(testVehicle);
        doThrow(new RuntimeException("RabbitMQ connection failed"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyMap());

        // Act & Assert - should not throw exception
        assertThatCode(() -> vehicleService.create(testVehicle))
                .doesNotThrowAnyException();
        
        verify(repository, times(1)).save(testVehicle);
    }

    @Test
    @DisplayName("Should transition status from AVAILABLE to MAINTENANCE")
    void changeStatus_FromAvailableToMaintenance_Success() {
        // Arrange
        testVehicle.setStatus(VehicleStatus.AVAILABLE);
        when(repository.findById(testVehicleId))
                .thenReturn(Optional.of(testVehicle));
        when(repository.save(any(Vehicle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Vehicle result = vehicleService.changeStatus(testVehicleId, VehicleStatus.MAINTENANCE);

        // Assert
        assertThat(result.getStatus()).isEqualTo(VehicleStatus.MAINTENANCE);
        verify(repository, times(1)).save(testVehicle);
    }

    @Test
    @DisplayName("Should maintain data integrity when updating vehicle fields")
    void update_MaintainsVehicleIdAndUserId() {
        // Arrange
        UUID originalId = testVehicleId;
        String originalUserId = "user-firebase-uid-123";
        testVehicle.setId(originalId);
        testVehicle.setUserId(originalUserId);
        
        Vehicle updatedData = new Vehicle();
        updatedData.setMake("Nissan");
        updatedData.setModel("Altima");

        when(repository.findById(originalId))
                .thenReturn(Optional.of(testVehicle));
        when(repository.save(any(Vehicle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Vehicle result = vehicleService.update(originalId, updatedData);

        // Assert - ID and UserID should remain unchanged
        assertThat(result.getId()).isEqualTo(originalId);
        assertThat(result.getUserId()).isEqualTo(originalUserId);
        assertThat(result.getMake()).isEqualTo("Nissan");
    }

    @Test
    @DisplayName("Should retrieve multiple vehicles for user with multiple vehicles")
    void getByUser_WithMultipleVehicles_ReturnsAllVehicles() {
        // Arrange
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setId(UUID.randomUUID());
        vehicle2.setUserId("user-firebase-uid-123");
        vehicle2.setMake("Honda");
        vehicle2.setModel("Civic");
        
        List<Vehicle> expectedVehicles = Arrays.asList(testVehicle, vehicle2);
        when(repository.findByUserId("user-firebase-uid-123"))
                .thenReturn(expectedVehicles);

        // Act
        List<Vehicle> result = vehicleService.getByUser("user-firebase-uid-123");

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(testVehicle, vehicle2);
    }

    @Test
    @DisplayName("Should transition status from IN_SERVICE back to AVAILABLE")
    void changeStatus_FromInServiceToAvailable_Success() {
        // Arrange
        testVehicle.setStatus(VehicleStatus.IN_SERVICE);
        when(repository.findById(testVehicleId))
                .thenReturn(Optional.of(testVehicle));
        when(repository.save(any(Vehicle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Vehicle result = vehicleService.changeStatus(testVehicleId, VehicleStatus.AVAILABLE);

        // Assert
        assertThat(result.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        
        ArgumentCaptor<Map> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("vehicle.exchange"),
                eq("vehicle.status.changed"),
                payloadCaptor.capture()
        );
        
        Map<String, Object> payload = payloadCaptor.getValue();
        assertThat(payload)
                .containsEntry("oldStatus", "IN_SERVICE")
                .containsEntry("newStatus", "AVAILABLE");
    }
}
