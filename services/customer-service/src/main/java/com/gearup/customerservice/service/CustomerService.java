package com.gearup.customerservice.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gearup.customerservice.domain.Customer;
import com.gearup.customerservice.domain.KycStatus;
import com.gearup.customerservice.repository.CustomerRepository;
import com.gearup.shared.exception.GearUpResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        Customer existing = repository.findById(uid)
                .orElseThrow(() -> new GearUpResourceNotFoundException("Customer", uid));
        // Partial update: only overwrite fields provided in the incoming object
        if (incoming.getEmail() != null) {
            existing.setEmail(incoming.getEmail());
        }
        if (incoming.getDisplayName() != null) {
            existing.setDisplayName(incoming.getDisplayName());
        }
        if (incoming.getPhone() != null) {
            existing.setPhone(incoming.getPhone());
        }
        if (incoming.getPhotoURL() != null) {
            existing.setPhotoURL(incoming.getPhotoURL());
        }
        if (incoming.getIdNumber() != null) {
            existing.setIdNumber(incoming.getIdNumber());
        }
        if (incoming.getAddress() != null) {
            existing.setAddress(incoming.getAddress());
        }
        if (incoming.getBirthday() != null) {
            existing.setBirthday(incoming.getBirthday());
        }
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
        Customer existing = repository.findById(uid)
                .orElseThrow(() -> new GearUpResourceNotFoundException("Customer", uid));
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