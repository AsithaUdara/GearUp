package com.gearup.customerservice.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gearup.customerservice.domain.Customer;
import com.gearup.customerservice.domain.KycStatus;
import com.gearup.customerservice.repository.CustomerRepository;
import com.gearup.shared.exception.GearUpResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Unit Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository repository;

    @Mock
    private CustomerEventPublisher eventPublisher;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setFirebaseUid("test-firebase-uid-123");
        testCustomer.setEmail("test@example.com");
        testCustomer.setDisplayName("Test User");
        testCustomer.setPhone("+1234567890");
        testCustomer.setPhotoURL("https://example.com/photo.jpg");
        testCustomer.setIdNumber("ID123456");
        testCustomer.setAddress("123 Test Street");
        testCustomer.setBirthday("1990-01-01");
        testCustomer.setKycStatus(KycStatus.PENDING);
    }

    @Test
    @DisplayName("Should retrieve customer by UID successfully")
    void get_WhenCustomerExists_ReturnsCustomer() {
        // Arrange
        when(repository.findById("test-firebase-uid-123"))
                .thenReturn(Optional.of(testCustomer));

        // Act
        Optional<Customer> result = customerService.get("test-firebase-uid-123");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testCustomer);
        verify(repository, times(1)).findById("test-firebase-uid-123");
    }

    @Test
    @DisplayName("Should return empty optional when customer does not exist")
    void get_WhenCustomerDoesNotExist_ReturnsEmpty() {
        // Arrange
        when(repository.findById("non-existent-uid"))
                .thenReturn(Optional.empty());

        // Act
        Optional<Customer> result = customerService.get("non-existent-uid");

        // Assert
        assertThat(result).isEmpty();
        verify(repository, times(1)).findById("non-existent-uid");
    }

    @Test
    @DisplayName("Should create customer and publish registration event")
    void create_WithValidCustomer_SavesAndPublishesEvent() {
        // Arrange
        when(repository.save(any(Customer.class))).thenReturn(testCustomer);

        // Act
        Customer result = customerService.create(testCustomer);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFirebaseUid()).isEqualTo("test-firebase-uid-123");
        verify(repository, times(1)).save(testCustomer);
        
        // Verify event published
        verify(eventPublisher, times(1)).publishCustomerRegisteredEvent(
                eq("test-firebase-uid-123"),
                eq("test@example.com"),
                eq("Test User"),
                eq("+1234567890")
        );
    }

    @Test
    @DisplayName("Should update customer successfully and publish update event")
    void update_WithExistingCustomer_UpdatesAndPublishesEvent() {
        // Arrange
        Customer updatedData = new Customer();
        updatedData.setEmail("updated@example.com");
        updatedData.setDisplayName("Updated Name");
        updatedData.setPhone("+9876543210");
        updatedData.setPhotoURL("https://example.com/new-photo.jpg");
        updatedData.setIdNumber("NEWID789");
        updatedData.setAddress("456 New Street");
        updatedData.setBirthday("1995-05-05");

        when(repository.findById("test-firebase-uid-123"))
                .thenReturn(Optional.of(testCustomer));
        when(repository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Customer result = customerService.update("test-firebase-uid-123", updatedData);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getDisplayName()).isEqualTo("Updated Name");
        assertThat(result.getPhone()).isEqualTo("+9876543210");
        assertThat(result.getIdNumber()).isEqualTo("NEWID789");
        
        verify(repository, times(1)).findById("test-firebase-uid-123");
        verify(repository, times(1)).save(testCustomer);
        verify(eventPublisher, times(1)).publishCustomerUpdatedEvent(
                eq("test-firebase-uid-123"),
                eq("updated@example.com"),
                eq("Updated Name"),
                eq("+9876543210"),
                eq("456 New Street")
        );
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent customer")
    void update_WithNonExistentCustomer_ThrowsException() {
        // Arrange
        Customer updatedData = new Customer();
        when(repository.findById("non-existent-uid"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.update("non-existent-uid", updatedData))
                .isInstanceOf(GearUpResourceNotFoundException.class);
        
        verify(repository, times(1)).findById("non-existent-uid");
        verify(repository, never()).save(any(Customer.class));
        // No event should be published for non-existent customer
    }

    @Test
    @DisplayName("Should update KYC status and publish KYC change event")
    void updateKyc_WithValidStatus_UpdatesAndPublishesEvent() {
        // Arrange
        testCustomer.setKycStatus(KycStatus.PENDING);
        when(repository.findById("test-firebase-uid-123"))
                .thenReturn(Optional.of(testCustomer));
        when(repository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Customer result = customerService.updateKyc("test-firebase-uid-123", KycStatus.VERIFIED);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getKycStatus()).isEqualTo(KycStatus.VERIFIED);
        
        verify(repository, times(1)).findById("test-firebase-uid-123");
        verify(repository, times(1)).save(testCustomer);
        
        // Verify KYC change event
        verify(eventPublisher, times(1)).publishCustomerKycChangedEvent(
                eq("test-firebase-uid-123"),
                eq("PENDING"),
                eq("VERIFIED"),
                anyString() // changedBy parameter
        );
    }

    @Test
    @DisplayName("Should handle EventPublisher failure gracefully without affecting operation")
    void create_WhenEventPublisherFails_StillSavesCustomer() {
        // Arrange
        when(repository.save(any(Customer.class))).thenReturn(testCustomer);
        // EventPublisher exceptions are caught internally, so operation continues

        // Act & Assert - should not throw exception
        assertThatCode(() -> customerService.create(testCustomer))
                .doesNotThrowAnyException();
        
        verify(repository, times(1)).save(testCustomer);
    }

    @Test
    @DisplayName("Should transition KYC from PENDING to REJECTED")
    void updateKyc_FromPendingToRejected_Success() {
        // Arrange
        testCustomer.setKycStatus(KycStatus.PENDING);
        when(repository.findById("test-firebase-uid-123"))
                .thenReturn(Optional.of(testCustomer));
        when(repository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Customer result = customerService.updateKyc("test-firebase-uid-123", KycStatus.REJECTED);

        // Assert
        assertThat(result.getKycStatus()).isEqualTo(KycStatus.REJECTED);
        verify(repository, times(1)).save(testCustomer);
    }

    @Test
    @DisplayName("Should maintain data integrity when updating customer fields")
    void update_MaintainsFirebaseUidAndOtherImmutableFields() {
        // Arrange
        String originalUid = "test-firebase-uid-123";
        testCustomer.setFirebaseUid(originalUid);
        
        Customer updatedData = new Customer();
        updatedData.setEmail("new@example.com");
        updatedData.setDisplayName("New Name");

        when(repository.findById(originalUid))
                .thenReturn(Optional.of(testCustomer));
        when(repository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Customer result = customerService.update(originalUid, updatedData);

        // Assert - Firebase UID should remain unchanged
        assertThat(result.getFirebaseUid()).isEqualTo(originalUid);
        assertThat(result.getEmail()).isEqualTo("new@example.com");
    }
}
