package com.gearup.modificationservice.repository;

import com.gearup.modificationservice.entity.Modification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ModificationRepository extends JpaRepository<Modification, UUID> {
    List<Modification> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Modification> findByVehicleIdOrderByCreatedAtDesc(String vehicleId);
    List<Modification> findByStatusOrderByCreatedAtDesc(String status);
}
