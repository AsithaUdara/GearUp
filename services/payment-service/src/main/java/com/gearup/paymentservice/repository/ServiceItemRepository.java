package com.gearup.paymentservice.repository;

import com.gearup.paymentservice.model.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceItemRepository extends JpaRepository<ServiceItem, UUID> {
    // Basic CRUD operations inherited from JpaRepository
    // Additional custom queries can be added here if needed
}
