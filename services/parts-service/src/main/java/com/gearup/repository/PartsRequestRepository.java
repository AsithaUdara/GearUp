package com.gearup.repository;

import com.gearup.domain.PartsRequest;
import com.gearup.domain.PartsRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PartsRequestRepository extends JpaRepository<PartsRequest, UUID> {
    Page<PartsRequest> findByCreatedByOrderByCreatedAtDesc(UUID createdBy, Pageable pageable);
    Page<PartsRequest> findByStatusOrderByCreatedAtDesc(PartsRequestStatus status, Pageable pageable);
}