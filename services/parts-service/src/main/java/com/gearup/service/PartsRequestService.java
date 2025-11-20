package com.gearup.service;

import com.gearup.domain.PartsRequest;
import com.gearup.domain.PartsRequestStatus;
import com.gearup.dto.CreatePartsRequestDTO;
import com.gearup.dto.PartsRequestDTO;
import com.gearup.events.PartsRequestEvent;
import com.gearup.repository.PartsRequestRepository;
import com.gearup.shared.messaging.EventPublisher;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PartsRequestService {
    private final PartsRequestRepository partsRequestRepository;
    private final EventPublisher eventPublisher;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Transactional
    public PartsRequestDTO createPartsRequest(CreatePartsRequestDTO dto, UUID userId) {
        PartsRequest partsRequest = new PartsRequest();
        partsRequest.setRequestId(generateRequestId());
        partsRequest.setMaterial(dto.getMaterial());
        partsRequest.setQuantity(dto.getQuantity());
        partsRequest.setNotes(dto.getNotes());
        partsRequest.setStatus(PartsRequestStatus.PENDING);
        partsRequest.setCreatedBy(userId);

        PartsRequest saved = partsRequestRepository.save(partsRequest);
        
        // Publish event
        PartsRequestEvent createdEvent = PartsRequestEvent.created(
            saved.getId(),
            saved.getRequestId(),
            saved.getMaterial(),
            saved.getQuantity(),
            userId
        );
        eventPublisher.publish(
            "parts.exchange",
            "parts.request.created",
            createdEvent,
            UUID.randomUUID().toString()
        );
        
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<PartsRequestDTO> getUserRequests(UUID userId, Pageable pageable) {
        return partsRequestRepository.findByCreatedByOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<PartsRequestDTO> getRequestsByStatus(PartsRequestStatus status, Pageable pageable) {
        return partsRequestRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<PartsRequestDTO> getAllRequests(Pageable pageable) {
        return partsRequestRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Transactional
    public PartsRequestDTO updateRequestStatus(UUID requestId, PartsRequestStatus newStatus, UUID approverId) {
        PartsRequest request = partsRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Parts request not found"));
        
        request.setStatus(newStatus);
        PartsRequest updated = partsRequestRepository.save(request);
        
        // Publish event
        PartsRequestEvent statusEvent = PartsRequestEvent.statusChanged(
            updated.getId(),
            updated.getRequestId(),
            newStatus,
            approverId
        );
        eventPublisher.publish(
            "parts.exchange",
            "parts.request.status.changed",
            statusEvent,
            UUID.randomUUID().toString()
        );
        
        return mapToDTO(updated);
    }

    private String generateRequestId() {
        return "REQ-" + LocalDateTime.now().getYear() + "-" 
                + String.format("%03d", (int) (Math.random() * 1000));
    }

    private PartsRequestDTO mapToDTO(PartsRequest entity) {
        PartsRequestDTO dto = new PartsRequestDTO();
        dto.setId(entity.getId());
        dto.setRequestId(entity.getRequestId());
        dto.setMaterial(entity.getMaterial());
        dto.setQuantity(entity.getQuantity());
        dto.setNotes(entity.getNotes());
        dto.setStatus(entity.getStatus());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setDate(entity.getCreatedAt().format(DATE_FORMATTER));
        dto.setCreatedAt(entity.getCreatedAt().toString());
        return dto;
    }
}