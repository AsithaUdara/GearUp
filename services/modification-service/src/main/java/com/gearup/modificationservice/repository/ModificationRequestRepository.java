package com.gearup.modificationservice.repository;

import com.gearup.modificationservice.entity.ModificationRequest;
import com.gearup.modificationservice.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModificationRequestRepository extends JpaRepository<ModificationRequest, Long> {
    
    List<ModificationRequest> findByServiceId(Long serviceId);
    
    List<ModificationRequest> findByCustomerId(Long customerId);
    
    List<ModificationRequest> findByStatus(RequestStatus status);
    
    @Query("SELECT mr FROM ModificationRequest mr WHERE mr.service.id = :serviceId ORDER BY mr.requestDate DESC")
    List<ModificationRequest> findByServiceIdOrderByRequestDateDesc(@Param("serviceId") Long serviceId);
    
    @Query("SELECT mr FROM ModificationRequest mr WHERE mr.customer.userId = :userId ORDER BY mr.requestDate DESC")
    List<ModificationRequest> findByCustomerUserIdOrderByRequestDateDesc(@Param("userId") String userId);
}