package com.gearup.trackingservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gearup.trackingservice.dto.request.CreatePartsRequest;
import com.gearup.trackingservice.dto.response.PartsRequestResponse;
import com.gearup.trackingservice.entity.PartsRequest;
import com.gearup.trackingservice.repository.PartsRequestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartsRequestService {
    
    private final PartsRequestRepository partsRequestRepository;
    
    @Transactional
    public PartsRequestResponse createPartsRequest(CreatePartsRequest request) {
        log.info("Creating parts request for: {}", request.getMaterial());
        
        String requestId = generateRequestId();
        
        PartsRequest partsRequest = PartsRequest.builder()
            .requestId(requestId)
            .material(request.getMaterial())
            .quantity(request.getQuantity())
            .status(request.getStatus())
            .date(request.getDate())
            .vehicle(request.getVehicle())
            .serviceId(request.getServiceId())
            .requestedBy(request.getRequestedBy())
            .notes(request.getNotes())
            .cost(request.getCost())
            .build();
            
        PartsRequest saved = partsRequestRepository.save(partsRequest);
        
        log.info("Parts request created: {}", requestId);
        
        return mapToResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public List<PartsRequestResponse> getPartsByVehicle(String vehicle) {
        log.info("Fetching parts requests for vehicle: {}", vehicle);
        
        return partsRequestRepository.findByVehicle(vehicle)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<PartsRequestResponse> getPartsByServiceId(String serviceId) {
        log.info("Fetching parts requests for service: {}", serviceId);
        
        return partsRequestRepository.findByServiceId(serviceId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    private String generateRequestId() {
        return "REQ-" + java.time.Year.now() + "-" + String.format("%03d", System.currentTimeMillis() % 1000);
    }
    
    private PartsRequestResponse mapToResponse(PartsRequest request) {
        return PartsRequestResponse.builder()
            .id(request.getId())
            .requestId(request.getRequestId())
            .material(request.getMaterial())
            .quantity(request.getQuantity())
            .status(request.getStatus())
            .date(request.getDate())
            .vehicle(request.getVehicle())
            .serviceId(request.getServiceId())
            .requestedBy(request.getRequestedBy())
            .notes(request.getNotes())
            .cost(request.getCost())
            .build();
    }
}
