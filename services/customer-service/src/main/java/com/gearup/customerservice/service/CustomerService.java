package com.gearup.customerservice.service;

import com.gearup.customerservice.domain.Customer;
import com.gearup.customerservice.domain.KycStatus;
import com.gearup.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;
    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "customer.exchange";

    public Optional<Customer> get(String uid) {
        return repository.findById(uid);
    }

    @Transactional
    public Customer create(Customer c) {
        Customer saved = repository.save(c);
        publishEvent("customer.registered", Map.of(
                "eventId", UUID.randomUUID().toString(),
                "customerId", saved.getFirebaseUid(),
                "email", saved.getEmail(),
                "displayName", saved.getDisplayName(),
                "timestamp", Instant.now().toString()
        ));
        return saved;
    }

    @Transactional
    public Customer update(String uid, Customer incoming) {
        Customer existing = repository.findById(uid).orElseThrow();
        existing.setEmail(incoming.getEmail());
        existing.setDisplayName(incoming.getDisplayName());
        existing.setPhone(incoming.getPhone());
        existing.setPhotoURL(incoming.getPhotoURL());
        existing.setIdNumber(incoming.getIdNumber());
        existing.setAddress(incoming.getAddress());
        existing.setBirthday(incoming.getBirthday());
        Customer saved = repository.save(existing);
        publishEvent("customer.updated", Map.of(
                "eventId", UUID.randomUUID().toString(),
                "customerId", saved.getFirebaseUid(),
                "timestamp", Instant.now().toString()
        ));
        return saved;
    }

    @Transactional
    public Customer updateKyc(String uid, KycStatus status) {
        Customer existing = repository.findById(uid).orElseThrow();
        KycStatus old = existing.getKycStatus();
        existing.setKycStatus(status);
        Customer saved = repository.save(existing);
        publishEvent("customer.kyc.changed", Map.of(
                "eventId", UUID.randomUUID().toString(),
                "customerId", saved.getFirebaseUid(),
                "oldStatus", old.name(),
                "newStatus", status.name(),
                "timestamp", Instant.now().toString()
        ));
        return saved;
    }

    private void publishEvent(String routingKey, Map<String, Object> payload) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, routingKey, payload);
        } catch (Exception ignored) {
        }
    }
}
