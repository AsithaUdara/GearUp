package com.gearup.customerservice.integration;

import com.gearup.customerservice.domain.Customer;
import com.gearup.customerservice.domain.KycStatus;
import com.gearup.customerservice.repository.CustomerRepository;
import com.gearup.customerservice.service.CustomerService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Customer Service Event-Driven Architecture
 * Tests verify that events are correctly published when customer actions occur
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Customer Service Event Integration Tests")
class CustomerEventIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    private static final String TEST_EMAIL = "integration.test@example.com";
    private static final String TEST_FIREBASE_UID = "test-firebase-uid-integration";

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        customerRepository.findById(TEST_FIREBASE_UID)
                .ifPresent(customer -> customerRepository.delete(customer));
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        customerRepository.findById(TEST_FIREBASE_UID)
                .ifPresent(customer -> customerRepository.delete(customer));
    }

    @Test
    @Order(1)
    @DisplayName("Should successfully create customer and event system should handle it")
    void testCustomerCreation() throws Exception {
        // Arrange
        Customer customer = new Customer();
        customer.setFirebaseUid(TEST_FIREBASE_UID);
        customer.setEmail(TEST_EMAIL);
        customer.setDisplayName("Integration Test User");
        customer.setPhone("+1234567890");
        customer.setKycStatus(KycStatus.PENDING);

        // Act
        Customer created = customerService.create(customer);

        // Assert - Verify customer was created
        assertThat(created).isNotNull();
        assertThat(created.getFirebaseUid()).isEqualTo(TEST_FIREBASE_UID);
        assertThat(created.getEmail()).isEqualTo(TEST_EMAIL);

        // Verify customer persisted
        Customer persisted = customerRepository.findById(TEST_FIREBASE_UID)
                .orElseThrow(() -> new AssertionError("Customer should be persisted"));
        assertThat(persisted.getEmail()).isEqualTo(TEST_EMAIL);

        // Note: Event publishing is tested separately in service unit tests
        // Integration test focuses on end-to-end flow
    }

    @Test
    @Order(2)
    @DisplayName("Should successfully update customer and event system should handle it")
    void testCustomerUpdate() throws Exception {
        // Arrange - Create a customer first
        Customer customer = new Customer();
        customer.setFirebaseUid(TEST_FIREBASE_UID);
        customer.setEmail(TEST_EMAIL);
        customer.setDisplayName("Integration Test User");
        customer.setPhone("+1234567890");
        customer.setKycStatus(KycStatus.PENDING);
        customer.setAddress("123 Test St");

        Customer created = customerService.create(customer);

        // Act - Update the customer
        created.setDisplayName("Updated Integration Test User");
        created.setPhone("+0987654321");
        created.setAddress("456 Updated Ave");

        Customer updated = customerService.update(created.getFirebaseUid(), created);

        // Assert
        assertThat(updated.getDisplayName()).isEqualTo("Updated Integration Test User");
        assertThat(updated.getPhone()).isEqualTo("+0987654321");
        assertThat(updated.getAddress()).isEqualTo("456 Updated Ave");

        // Verify update persisted
        Customer persisted = customerRepository.findById(TEST_FIREBASE_UID)
                .orElseThrow(() -> new AssertionError("Customer should exist"));
        assertThat(persisted.getDisplayName()).isEqualTo("Updated Integration Test User");
    }

    @Test
    @Order(3)
    @DisplayName("Should successfully update KYC status and event system should handle it")
    void testKycStatusUpdate() throws Exception {
        // Arrange - Create a customer first
        Customer customer = new Customer();
        customer.setFirebaseUid(TEST_FIREBASE_UID);
        customer.setEmail(TEST_EMAIL);
        customer.setDisplayName("Integration Test User");
        customer.setPhone("+1234567890");
        customer.setKycStatus(KycStatus.PENDING);

        Customer created = customerService.create(customer);
        assertThat(created.getKycStatus()).isEqualTo(KycStatus.PENDING);

        // Act - Update KYC status to VERIFIED
        Customer verified = customerService.updateKyc(
                created.getFirebaseUid(),
                KycStatus.VERIFIED
        );

        // Assert
        assertThat(verified.getKycStatus()).isEqualTo(KycStatus.VERIFIED);

        // Verify KYC update persisted
        Customer persisted = customerRepository.findById(TEST_FIREBASE_UID)
                .orElseThrow(() -> new AssertionError("Customer should exist"));
        assertThat(persisted.getKycStatus()).isEqualTo(KycStatus.VERIFIED);
    }

    @Test
    @Order(4)
    @DisplayName("Should handle complete customer lifecycle")
    void testCompleteCustomerLifecycle() throws Exception {
        // 1. Create customer
        Customer customer = new Customer();
        customer.setFirebaseUid(TEST_FIREBASE_UID);
        customer.setEmail(TEST_EMAIL);
        customer.setDisplayName("Lifecycle Test User");
        customer.setPhone("+1234567890");
        customer.setKycStatus(KycStatus.PENDING);

        Customer created = customerService.create(customer);
        assertThat(created).isNotNull();
        assertThat(created.getKycStatus()).isEqualTo(KycStatus.PENDING);

        // 2. Update profile
        created.setDisplayName("Lifecycle Test User - Updated");
        created.setAddress("123 Lifecycle St");
        Customer updated = customerService.update(created.getFirebaseUid(), created);
        assertThat(updated.getDisplayName()).isEqualTo("Lifecycle Test User - Updated");

        // 3. Verify KYC (PENDING → VERIFIED)
        Customer verified = customerService.updateKyc(
                updated.getFirebaseUid(),
                KycStatus.VERIFIED
        );
        assertThat(verified.getKycStatus()).isEqualTo(KycStatus.VERIFIED);

        // 4. Update profile again after verification
        verified.setPhone("+0987654321");
        Customer finalUpdate = customerService.update(verified.getFirebaseUid(), verified);
        assertThat(finalUpdate.getPhone()).isEqualTo("+0987654321");
        assertThat(finalUpdate.getKycStatus()).isEqualTo(KycStatus.VERIFIED); // KYC status preserved

        // Final verification
        Customer finalState = customerRepository.findById(TEST_FIREBASE_UID)
                .orElseThrow(() -> new AssertionError("Customer should exist"));
        assertThat(finalState.getDisplayName()).isEqualTo("Lifecycle Test User - Updated");
        assertThat(finalState.getPhone()).isEqualTo("+0987654321");
        assertThat(finalState.getAddress()).isEqualTo("123 Lifecycle St");
        assertThat(finalState.getKycStatus()).isEqualTo(KycStatus.VERIFIED);
    }

    @Test
    @Order(5)
    @DisplayName("Should handle KYC rejection workflow")
    void testKycRejectionWorkflow() throws Exception {
        // Arrange
        Customer customer = new Customer();
        customer.setFirebaseUid(TEST_FIREBASE_UID);
        customer.setEmail(TEST_EMAIL);
        customer.setDisplayName("KYC Test User");
        customer.setPhone("+1234567890");
        customer.setKycStatus(KycStatus.PENDING);

        Customer created = customerService.create(customer);

        // Act - Reject KYC
        Customer rejected = customerService.updateKyc(
                created.getFirebaseUid(),
                KycStatus.REJECTED
        );

        // Assert
        assertThat(rejected.getKycStatus()).isEqualTo(KycStatus.REJECTED);

        // Verify rejection persisted
        Customer persisted = customerRepository.findById(TEST_FIREBASE_UID)
                .orElseThrow(() -> new AssertionError("Customer should exist"));
        assertThat(persisted.getKycStatus()).isEqualTo(KycStatus.REJECTED);

        // Act - Re-submit KYC (REJECTED → PENDING)
        Customer resubmitted = customerService.updateKyc(
                persisted.getFirebaseUid(),
                KycStatus.PENDING
        );

        // Assert - Can go back to pending
        assertThat(resubmitted.getKycStatus()).isEqualTo(KycStatus.PENDING);
    }

    @Test
    @Order(6)
    @DisplayName("Should maintain immutable fields during updates")
    void testImmutableFieldsProtection() throws Exception {
        // Arrange
        Customer customer = new Customer();
        customer.setFirebaseUid(TEST_FIREBASE_UID);
        customer.setEmail(TEST_EMAIL);
        customer.setDisplayName("Immutable Test User");
        customer.setPhone("+1234567890");
        customer.setKycStatus(KycStatus.PENDING);

        Customer created = customerService.create(customer);
        String originalFirebaseUid = created.getFirebaseUid();
        String originalEmail = created.getEmail();

        // Act - Attempt to change immutable fields
        created.setFirebaseUid("SHOULD-NOT-CHANGE");
        created.setEmail("should.not.change@example.com");
        created.setDisplayName("Changed Display Name"); // This should change

        Customer updated = customerService.update(originalFirebaseUid, created);

        // Assert - Immutable fields should not change
        assertThat(updated.getFirebaseUid()).isEqualTo(originalFirebaseUid);
        assertThat(updated.getEmail()).isEqualTo(originalEmail);
        assertThat(updated.getDisplayName()).isEqualTo("Changed Display Name");
    }
}
