package com.gearup.modificationservice.service;

import com.gearup.modificationservice.dto.*;
import com.gearup.modificationservice.entity.Customer;
import com.gearup.modificationservice.entity.ModificationRequest;
import com.gearup.modificationservice.entity.ModificationService;
import com.gearup.modificationservice.entity.RequestStatus;
import com.gearup.modificationservice.repository.CustomerRepository;
import com.gearup.modificationservice.repository.ModificationRequestRepository;
import com.gearup.modificationservice.repository.ModificationServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModificationRequestService {
    
    private final ModificationRequestRepository modificationRequestRepository;
    private final ModificationServiceRepository modificationServiceRepository;
    private final CustomerRepository customerRepository;
    
    @Transactional
    public ModificationRequestDTO createRequest(CreateModificationRequestDTO request) {
        log.debug("Creating modification request for service: {}", request.getServiceId());
        
        try {
            // Validate service exists
            ModificationService service = modificationServiceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new RuntimeException("Service not found"));
            
            // Create or get customer
            // For testing, use email as userId if no auth context
            String userId = request.getCustomerEmail() != null ? request.getCustomerEmail() : "test-user-" + System.currentTimeMillis();
            
            Customer customer = customerRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        Customer newCustomer = new Customer();
                        newCustomer.setUserId(userId);
                        newCustomer.setName(request.getCustomerName());
                        newCustomer.setEmail(request.getCustomerEmail());
                        newCustomer.setPhone(request.getCustomerPhone());
                        newCustomer.setAddress(request.getCustomerAddress());
                        return customerRepository.save(newCustomer);
                    });
            
            // Create modification request
            ModificationRequest modificationRequest = new ModificationRequest();
            modificationRequest.setService(service);
            modificationRequest.setCustomer(customer);
            modificationRequest.setStatus(RequestStatus.PENDING);
            modificationRequest.setRequestDate(LocalDateTime.now());
            
            // Parse preferred date if provided
            if (request.getPreferredDate() != null && !request.getPreferredDate().isEmpty()) {
                try {
                    modificationRequest.setPreferredDate(LocalDate.parse(request.getPreferredDate()));
                } catch (Exception e) {
                    log.warn("Failed to parse preferred date: {}", request.getPreferredDate());
                    // Continue without preferred date
                }
            }
            
            modificationRequest.setNotes(request.getNotes());
            modificationRequest.setEstimatedCost(service.getBasePrice());
            
            modificationRequest = modificationRequestRepository.save(modificationRequest);
            
            log.info("Modification request created successfully with id: {}", modificationRequest.getId());
            return convertToDTO(modificationRequest);
        } catch (Exception e) {
            log.error("Error creating modification request: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create modification request: " + e.getMessage());
        }
    }
    
    public List<ModificationRequestDTO> getRequestsByServiceId(Long serviceId) {
        log.debug("Fetching modification requests for service: {}", serviceId);
        try {
            List<ModificationRequest> requests = modificationRequestRepository.findByServiceIdOrderByRequestDateDesc(serviceId);
            return requests.stream()
                    .map(this::convertToDTO)
                    .toList();
        } catch (Exception e) {
            log.error("Error fetching requests for service {}: {}", serviceId, e.getMessage());
            return List.of(); // Return empty list on error
        }
    }
    
    public Optional<ModificationRequestDTO> getRequestById(Long id) {
        log.debug("Fetching modification request with id: {}", id);
        return modificationRequestRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    @Transactional
    public ModificationRequestDTO updateRequest(Long id, UpdateModificationRequestDTO updateDTO) {
        log.debug("Updating modification request with id: {}", id);
        
        ModificationRequest request = modificationRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Modification request not found"));
        
        if (updateDTO.getStatus() != null) {
            RequestStatus oldStatus = request.getStatus();
            request.setStatus(updateDTO.getStatus());
            
            // Update timestamp based on status
            if (updateDTO.getStatus() == RequestStatus.APPROVED && oldStatus != RequestStatus.APPROVED) {
                request.setApprovedAt(LocalDateTime.now());
            } else if (updateDTO.getStatus() == RequestStatus.REJECTED && oldStatus != RequestStatus.REJECTED) {
                request.setRejectedAt(LocalDateTime.now());
            } else if (updateDTO.getStatus() == RequestStatus.COMPLETED && oldStatus != RequestStatus.COMPLETED) {
                request.setCompletedAt(LocalDateTime.now());
            }
        }
        
        if (updateDTO.getNotes() != null) {
            request.setNotes(updateDTO.getNotes());
        }
        if (updateDTO.getAdminNotes() != null) {
            request.setAdminNotes(updateDTO.getAdminNotes());
        }
        if (updateDTO.getEstimatedCost() != null) {
            request.setEstimatedCost(updateDTO.getEstimatedCost());
        }
        if (updateDTO.getFinalCost() != null) {
            request.setFinalCost(updateDTO.getFinalCost());
        }
        
        request = modificationRequestRepository.save(request);
        log.info("Modification request updated successfully with id: {}", request.getId());
        return convertToDTO(request);
    }
    
    @Transactional
    public void deleteRequest(Long id) {
        log.debug("Deleting modification request with id: {}", id);
        
        if (!modificationRequestRepository.existsById(id)) {
            throw new RuntimeException("Modification request not found");
        }
        
        modificationRequestRepository.deleteById(id);
        log.info("Modification request deleted successfully with id: {}", id);
    }
    
    public List<ModificationServiceDTO> getServicesByCustomerId(String customerId) {
        log.debug("Fetching services for customer: {}", customerId);
        
        Customer customer = customerRepository.findByUserId(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        List<ModificationRequest> requests = modificationRequestRepository.findByCustomerId(customer.getId());
        
        return requests.stream()
                .map(req -> {
                    ModificationService service = req.getService();
                    return new ModificationServiceDTO(
                            service.getId(),
                            service.getName(),
                            service.getDescription(),
                            service.getBasePrice(),
                            service.getEstimatedDurationHours(),
                            service.getIsActive()
                    );
                })
                .distinct()
                .toList();
    }
    
    private ModificationRequestDTO convertToDTO(ModificationRequest request) {
        return new ModificationRequestDTO(
                request.getId(),
                request.getService().getId(),
                request.getService().getName(),
                request.getCustomer().getId(),
                request.getCustomer().getName(),
                request.getStatus(),
                request.getRequestDate(),
                request.getPreferredDate(),
                request.getNotes(),
                request.getAdminNotes(),
                request.getEstimatedCost(),
                request.getFinalCost(),
                request.getApprovedAt(),
                request.getRejectedAt(),
                request.getCompletedAt()
        );
    }
}