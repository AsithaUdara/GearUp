package com.gearup.modificationservice.repository;

import com.gearup.modificationservice.entity.ModificationService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModificationServiceRepository extends JpaRepository<ModificationService, Long> {
    
    List<ModificationService> findByActiveTrue();
    
    @Query("SELECT s FROM ModificationService s WHERE s.active = true ORDER BY s.name")
    List<ModificationService> findActiveServicesOrderByName();
}