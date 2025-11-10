package com.gearup.customerservice.service;

import com.gearup.customerservice.domain.Customer;
import com.gearup.customerservice.domain.KycStatus;
import com.gearup.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository repository;
    private final CustomerEventPublisher eventPublisher;

    public Optional<Customer> get(String uid) {
        return repository.findById(uid);
    }

    @Transactional
    public Customer create(Customer c) {
        Customer saved = repository.save(c);
        
        eventPublisher.publishCustomerRegisteredEvent(
            saved.getFirebaseUid(),
            saved.getEmail(),
            saved.getDisplayName(),
            saved.getPhone()
        );
        
        log.info("📢 Created customer and published event: {}", saved.getFirebaseUid());
        return saved;
    }

    @Transactional
    public Customer update(String uid, Customer incoming) {
        Customer existing = repository.findById(uid).orElseThrow();
        // Note: Email and firebaseUid are immutable and should not be updated
        existing.setDisplayName(incoming.getDisplayName());
        existing.setPhone(incoming.getPhone());
        existing.setPhotoURL(incoming.getPhotoURL());
        existing.setIdNumber(incoming.getIdNumber());
        existing.setAddress(incoming.getAddress());
        existing.setBirthday(incoming.getBirthday());
        Customer saved = repository.save(existing);
        
        eventPublisher.publishCustomerUpdatedEvent(
            saved.getFirebaseUid(),
            saved.getEmail(),
            saved.getDisplayName(),
            saved.getPhone(),
            saved.getAddress()
        );
        
        log.info("📢 Updated customer and published event: {}", saved.getFirebaseUid());
        return saved;
    }

    @Transactional
    public Customer updateKyc(String uid, KycStatus status) {
        Customer existing = repository.findById(uid).orElseThrow();
        KycStatus old = existing.getKycStatus();
        existing.setKycStatus(status);
        Customer saved = repository.save(existing);
        
        eventPublisher.publishCustomerKycChangedEvent(
            saved.getFirebaseUid(),
            old.name(),
            status.name(),
            "KYC status updated by system"
        );
        
        log.info("📢 Updated KYC and published event: {} -> {}", old, status);
        return saved;
    }
}