package com.gearup.trackingservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gearup.trackingservice.entity.PartsRequest;

@Repository
public interface PartsRequestRepository extends JpaRepository<PartsRequest, Long> {
    
    Optional<PartsRequest> findByRequestId(String requestId);
    
    List<PartsRequest> findByVehicle(String vehicle);
    
    List<PartsRequest> findByServiceId(String serviceId);
    
    List<PartsRequest> findByStatus(PartsRequest.RequestStatus status);
    
    List<PartsRequest> findByRequestedBy(String requestedBy);
    
    List<PartsRequest> findByVehicleAndServiceId(String vehicle, String serviceId);
}
