package com.gearup.trackingservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gearup.trackingservice.entity.ServiceProgress;

@Repository
public interface ServiceProgressRepository extends JpaRepository<ServiceProgress, Long> {
    
    Optional<ServiceProgress> findByProgressId(String progressId);
    
    Optional<ServiceProgress> findByServiceId(String serviceId);
    
    List<ServiceProgress> findByCustomerName(String customerName);
    
    List<ServiceProgress> findByTechnicianId(String technicianId);
    
    List<ServiceProgress> findByOverallStatus(ServiceProgress.OverallStatus status);
    
    @Query("SELECT s FROM ServiceProgress s WHERE s.technicianId = :technicianId AND s.overallStatus IN ('scheduled', 'in_progress') ORDER BY s.appointmentDate ASC")
    List<ServiceProgress> findActiveServicesByTechnician(@Param("technicianId") String technicianId);
}
