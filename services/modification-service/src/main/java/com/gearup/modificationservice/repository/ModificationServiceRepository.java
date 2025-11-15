package com.gearup.modificationservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gearup.modificationservice.entity.ModificationService;

@Repository
public interface ModificationServiceRepository extends JpaRepository<ModificationService, Long> {
    
    List<ModificationService> findByIsActiveTrue();
    
    @Query("SELECT s FROM ModificationService s WHERE s.isActive = true ORDER BY s.name")
    List<ModificationService> findActiveServicesOrderByName();
}