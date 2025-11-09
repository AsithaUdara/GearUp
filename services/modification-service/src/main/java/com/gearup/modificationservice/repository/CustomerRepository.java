package com.gearup.modificationservice.repository;

import com.gearup.modificationservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByUserId(String userId);
    
    boolean existsByUserId(String userId);
}