package com.gearup.appointmentservice.repository;

import com.gearup.appointmentservice.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    
    List<Service> findByIsActiveTrue();
    
    @Query("SELECT s FROM Service s WHERE s.isActive = true ORDER BY s.name")
    List<Service> findActiveServicesOrderByName();
}