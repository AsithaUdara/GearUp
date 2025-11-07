package com.gearup.customerservice.repository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import com.gearup.customerservice.domain.Customer;
import com.gearup.customerservice.domain.KycStatus;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("CustomerRepository Integration Tests")
class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

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
    @DisplayName("Should save and retrieve customer by Firebase UID")
    void saveAndFindById_Success() {
        // Act
        Customer saved = customerRepository.save(testCustomer);
        entityManager.flush();
        entityManager.clear();

        Optional<Customer> found = customerRepository.findById("test-firebase-uid-123");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getFirebaseUid()).isEqualTo("test-firebase-uid-123");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getDisplayName()).isEqualTo("Test User");
        assertThat(found.get().getKycStatus()).isEqualTo(KycStatus.PENDING);
    }

    @Test
    @DisplayName("Should return empty optional when customer not found")
    void findById_WhenNotExists_ReturnsEmpty() {
        // Act
        Optional<Customer> found = customerRepository.findById("non-existent-uid");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should update customer successfully")
    void updateCustomer_Success() {
        // Arrange
        Customer saved = customerRepository.save(testCustomer);
        entityManager.flush();
        entityManager.clear();

        // Act
        Customer toUpdate = customerRepository.findById("test-firebase-uid-123").orElseThrow();
        toUpdate.setEmail("updated@example.com");
        toUpdate.setDisplayName("Updated Name");
        toUpdate.setKycStatus(KycStatus.VERIFIED);
        
        Customer updated = customerRepository.save(toUpdate);
        entityManager.flush();
        entityManager.clear();

        Customer retrieved = customerRepository.findById("test-firebase-uid-123").orElseThrow();

        // Assert
        assertThat(retrieved.getEmail()).isEqualTo("updated@example.com");
        assertThat(retrieved.getDisplayName()).isEqualTo("Updated Name");
        assertThat(retrieved.getKycStatus()).isEqualTo(KycStatus.VERIFIED);
    }

    @Test
    @DisplayName("Should delete customer successfully")
    void deleteCustomer_Success() {
        // Arrange
        customerRepository.save(testCustomer);
        entityManager.flush();
        entityManager.clear();

        // Act
        customerRepository.deleteById("test-firebase-uid-123");
        entityManager.flush();
        entityManager.clear();

        Optional<Customer> found = customerRepository.findById("test-firebase-uid-123");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should count all customers correctly")
    void countCustomers_Success() {
        // Arrange
        customerRepository.save(testCustomer);
        
        Customer customer2 = new Customer();
        customer2.setFirebaseUid("test-firebase-uid-456");
        customer2.setEmail("test2@example.com");
        customer2.setDisplayName("Test User 2");
        customer2.setKycStatus(KycStatus.NOT_STARTED);
        customerRepository.save(customer2);
        
        entityManager.flush();

        // Act
        long count = customerRepository.count();

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should verify customer exists by ID")
    void existsById_ReturnsTrue() {
        // Arrange
        customerRepository.save(testCustomer);
        entityManager.flush();

        // Act
        boolean exists = customerRepository.existsById("test-firebase-uid-123");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when customer does not exist")
    void existsById_WhenNotExists_ReturnsFalse() {
        // Act
        boolean exists = customerRepository.existsById("non-existent-uid");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should persist all customer fields correctly")
    void saveCustomer_PersistsAllFields() {
        // Arrange
        testCustomer.setPhone("+9876543210");
        testCustomer.setPhotoURL("https://example.com/new-photo.jpg");
        testCustomer.setIdNumber("NEWID789");
        testCustomer.setAddress("456 New Avenue");
        testCustomer.setBirthday("1995-05-05");

        // Act
        Customer saved = customerRepository.save(testCustomer);
        entityManager.flush();
        entityManager.clear();

        Customer retrieved = customerRepository.findById("test-firebase-uid-123").orElseThrow();

        // Assert
        assertThat(retrieved.getPhone()).isEqualTo("+9876543210");
        assertThat(retrieved.getPhotoURL()).isEqualTo("https://example.com/new-photo.jpg");
        assertThat(retrieved.getIdNumber()).isEqualTo("NEWID789");
        assertThat(retrieved.getAddress()).isEqualTo("456 New Avenue");
        assertThat(retrieved.getBirthday()).isEqualTo("1995-05-05");
    }

    @Test
    @DisplayName("Should handle different KYC statuses")
    void saveCustomer_WithDifferentKycStatuses() {
        // Test NOT_STARTED
        Customer customer1 = new Customer();
        customer1.setFirebaseUid("uid-1");
        customer1.setEmail("user1@example.com");
        customer1.setKycStatus(KycStatus.NOT_STARTED);
        customerRepository.save(customer1);

        // Test PENDING
        Customer customer2 = new Customer();
        customer2.setFirebaseUid("uid-2");
        customer2.setEmail("user2@example.com");
        customer2.setKycStatus(KycStatus.PENDING);
        customerRepository.save(customer2);

        // Test VERIFIED
        Customer customer3 = new Customer();
        customer3.setFirebaseUid("uid-3");
        customer3.setEmail("user3@example.com");
        customer3.setKycStatus(KycStatus.VERIFIED);
        customerRepository.save(customer3);

        // Test REJECTED
        Customer customer4 = new Customer();
        customer4.setFirebaseUid("uid-4");
        customer4.setEmail("user4@example.com");
        customer4.setKycStatus(KycStatus.REJECTED);
        customerRepository.save(customer4);

        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(customerRepository.findById("uid-1").orElseThrow().getKycStatus())
                .isEqualTo(KycStatus.NOT_STARTED);
        assertThat(customerRepository.findById("uid-2").orElseThrow().getKycStatus())
                .isEqualTo(KycStatus.PENDING);
        assertThat(customerRepository.findById("uid-3").orElseThrow().getKycStatus())
                .isEqualTo(KycStatus.VERIFIED);
        assertThat(customerRepository.findById("uid-4").orElseThrow().getKycStatus())
                .isEqualTo(KycStatus.REJECTED);
    }

    @Test
    @DisplayName("Should automatically set timestamps on save")
    void saveCustomer_SetsTimestamps() {
        // Act
        Customer saved = customerRepository.save(testCustomer);
        entityManager.flush();

        // Assert
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should retrieve all customers")
    void findAll_ReturnsAllCustomers() {
        // Arrange
        customerRepository.save(testCustomer);
        
        Customer customer2 = new Customer();
        customer2.setFirebaseUid("uid-999");
        customer2.setEmail("another@example.com");
        customer2.setDisplayName("Another User");
        customer2.setKycStatus(KycStatus.VERIFIED);
        customerRepository.save(customer2);
        
        entityManager.flush();

        // Act
        var allCustomers = customerRepository.findAll();

        // Assert
        assertThat(allCustomers).hasSize(2);
        assertThat(allCustomers).extracting(Customer::getFirebaseUid)
                .containsExactlyInAnyOrder("test-firebase-uid-123", "uid-999");
    }
}
