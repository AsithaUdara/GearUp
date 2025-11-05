package com.gearup.trackingservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gearup.trackingservice.entity.ModificationRequest;

@Repository
public interface ModificationRequestRepository extends JpaRepository<ModificationRequest, Long> {
    
    Optional<ModificationRequest> findByRequestId(String requestId);
    
    List<ModificationRequest> findByVehicle(String vehicle);
    
    List<ModificationRequest> findByServiceId(String serviceId);
    
    @Query("SELECT m FROM ModificationRequest m WHERE m.status IN ('pending', 'approved') AND (m.assignedToEmployeeId IS NULL OR m.assignedToEmployeeId = :employeeId) ORDER BY m.requestedAt DESC")
    List<ModificationRequest> findPendingRequestsForEmployee(@Param("employeeId") String employeeId);
    
    List<ModificationRequest> findByStatusAndAssignedToEmployeeId(
        ModificationRequest.RequestStatus status, 
        String employeeId
    );
    
    List<ModificationRequest> findByStatus(ModificationRequest.RequestStatus status);
    
    @Query("SELECT m FROM ModificationRequest m WHERE m.requestedBy = :customerId ORDER BY m.requestedAt DESC")
    List<ModificationRequest> findByCustomerId(@Param("customerId") String customerId);
}
