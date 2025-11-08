package com.gearup.trackingservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gearup.trackingservice.dto.request.CreateModificationRequest;
import com.gearup.trackingservice.dto.request.CreateTaskRequest;
import com.gearup.trackingservice.dto.response.ModificationRequestResponse;
import com.gearup.trackingservice.dto.response.TaskResponse;
import com.gearup.trackingservice.entity.ModificationRequest;
import com.gearup.trackingservice.repository.ModificationRequestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModificationRequestService {
    
    private final ModificationRequestRepository modificationRequestRepository;
    private final TaskService taskService;
    
    @Transactional
    public ModificationRequestResponse createModificationRequest(CreateModificationRequest request) {
        log.info("Creating modification request for service: {}", request.getServiceId());
        
        String requestId = generateRequestId();
        
        ModificationRequest modRequest = ModificationRequest.builder()
            .requestId(requestId)
            .serviceId(request.getServiceId())
            .vehicle(request.getVehicle())
            .customer(request.getCustomer())
            .type(request.getType())
            .title(request.getTitle())
            .description(request.getDescription())
            .status(ModificationRequest.RequestStatus.pending)
            .requestedBy(request.getRequestedBy())
            .assignedToEmployeeId(request.getAssignedToEmployeeId())
            .estimatedCost(request.getEstimatedCost())
            .estimatedDuration(request.getEstimatedDuration())
            .requestedAt(LocalDateTime.now())
            .build();
            
        ModificationRequest saved = modificationRequestRepository.save(modRequest);
        
        log.info("Modification request created: {}", requestId);
        
        return mapToResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public List<ModificationRequestResponse> getPendingRequestsForEmployee(String employeeId) {
        log.info("Fetching pending modification requests for employee: {}", employeeId);
        
        return modificationRequestRepository.findPendingRequestsForEmployee(employeeId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ModificationRequestResponse> getRequestsByVehicle(String vehicle) {
        log.info("Fetching modification requests for vehicle: {}", vehicle);
        
        return modificationRequestRepository.findByVehicle(vehicle)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public TaskResponse createTaskFromRequest(String requestId, String employeeId) {
        log.info("Creating task from modification request: {}", requestId);
        
        ModificationRequest request = modificationRequestRepository.findByRequestId(requestId)
            .orElseThrow(() -> new RuntimeException("Modification request not found: " + requestId));
            
        // Create task
        CreateTaskRequest taskRequest = CreateTaskRequest.builder()
            .serviceId(request.getServiceId())
            .vehicle(request.getVehicle())
            .customer(request.getCustomer())
            .serviceType(request.getTitle())
            .assigneeId(employeeId)
            .estimatedDuration(request.getEstimatedDuration())
            .notes(request.getDescription())
            .build();
            
        TaskResponse taskResponse = taskService.createTask(taskRequest);
        
        // Update modification request
        request.setStatus(ModificationRequest.RequestStatus.in_progress);
        request.setAssignedToEmployeeId(employeeId);
        modificationRequestRepository.save(request);
        
        log.info("Task created from modification request: {}", taskResponse.getTaskId());
        
        return taskResponse;
    }
    
    private String generateRequestId() {
        return "mod-" + System.currentTimeMillis();
    }
    
    private ModificationRequestResponse mapToResponse(ModificationRequest request) {
        return ModificationRequestResponse.builder()
            .id(request.getId())
            .requestId(request.getRequestId())
            .serviceId(request.getServiceId())
            .vehicle(request.getVehicle())
            .customer(request.getCustomer())
            .type(request.getType())
            .title(request.getTitle())
            .description(request.getDescription())
            .status(request.getStatus())
            .requestedBy(request.getRequestedBy())
            .assignedToEmployeeId(request.getAssignedToEmployeeId())
            .estimatedCost(request.getEstimatedCost())
            .estimatedDuration(request.getEstimatedDuration())
            .requestedAt(request.getRequestedAt())
            .approvedAt(request.getApprovedAt())
            .completedAt(request.getCompletedAt())
            .build();
    }
}
