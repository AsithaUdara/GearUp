package com.gearup.trackingservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gearup.trackingservice.dto.request.CreateModificationRequest;
import com.gearup.trackingservice.dto.request.CreatePartsRequest;
import com.gearup.trackingservice.dto.response.DailySummaryResponse;
import com.gearup.trackingservice.dto.response.ModificationRequestResponse;
import com.gearup.trackingservice.dto.response.PartsRequestResponse;
import com.gearup.trackingservice.dto.response.TaskListResponse;
import com.gearup.trackingservice.dto.response.TaskResponse;
import com.gearup.trackingservice.service.ModificationRequestService;
import com.gearup.trackingservice.service.PartsRequestService;
import com.gearup.trackingservice.service.TaskService;
import com.gearup.trackingservice.service.TrackingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Slf4j
public class TrackingController {
    
    private final TaskService taskService;
    private final ModificationRequestService modificationRequestService;
    private final PartsRequestService partsRequestService;
    private final TrackingService trackingService;
    
    /**
     * Get all tasks for an employee
     * GET /api/tracking/employee/{employeeId}/tasks
     */
    @GetMapping("/employee/{employeeId}/tasks")
    public ResponseEntity<TaskListResponse> getEmployeeTasks(@PathVariable String employeeId) {
        log.info("Fetching tasks for employee: {}", employeeId);
        TaskListResponse response = taskService.getEmployeeTasks(employeeId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get daily summary for employee
     * GET /api/tracking/employee/{employeeId}/daily-summary
     */
    @GetMapping("/employee/{employeeId}/daily-summary")
    public ResponseEntity<DailySummaryResponse> getDailySummary(@PathVariable String employeeId) {
        log.info("Fetching daily summary for employee: {}", employeeId);
        DailySummaryResponse response = trackingService.getDailySummary(employeeId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get pending modification requests for employee
     * GET /api/tracking/employee/{employeeId}/modification-requests/pending
     */
    @GetMapping("/employee/{employeeId}/modification-requests/pending")
    public ResponseEntity<List<ModificationRequestResponse>> getPendingRequests(@PathVariable String employeeId) {
        log.info("Fetching pending modification requests for employee: {}", employeeId);
        List<ModificationRequestResponse> requests = modificationRequestService.getPendingRequestsForEmployee(employeeId);
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Create task from modification request
     * POST /api/tracking/modification-requests/{requestId}/create-task
     */
    @PostMapping("/modification-requests/{requestId}/create-task")
    public ResponseEntity<TaskResponse> createTaskFromRequest(
            @PathVariable String requestId,
            @RequestParam String employeeId) {
        log.info("Creating task from modification request: {} for employee: {}", requestId, employeeId);
        TaskResponse response = modificationRequestService.createTaskFromRequest(requestId, employeeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Create modification request
     * POST /api/tracking/modification-requests
     */
    @PostMapping("/modification-requests")
    public ResponseEntity<ModificationRequestResponse> createModificationRequest(
            @Valid @RequestBody CreateModificationRequest request) {
        log.info("Creating modification request for service: {}", request.getServiceId());
        ModificationRequestResponse response = modificationRequestService.createModificationRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get modification requests by vehicle
     * GET /api/tracking/vehicle/{vehicle}/modification-requests
     */
    @GetMapping("/vehicle/{vehicle}/modification-requests")
    public ResponseEntity<List<ModificationRequestResponse>> getModificationRequestsByVehicle(@PathVariable String vehicle) {
        log.info("Fetching modification requests for vehicle: {}", vehicle);
        List<ModificationRequestResponse> requests = modificationRequestService.getRequestsByVehicle(vehicle);
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Create parts request
     * POST /api/tracking/parts-requests
     */
    @PostMapping("/parts-requests")
    public ResponseEntity<PartsRequestResponse> createPartsRequest(@Valid @RequestBody CreatePartsRequest request) {
        log.info("Creating parts request for material: {}", request.getMaterial());
        PartsRequestResponse response = partsRequestService.createPartsRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get parts requests by vehicle
     * GET /api/tracking/vehicle/{vehicle}/parts-requests
     */
    @GetMapping("/vehicle/{vehicle}/parts-requests")
    public ResponseEntity<List<PartsRequestResponse>> getPartsRequestsByVehicle(@PathVariable String vehicle) {
        log.info("Fetching parts requests for vehicle: {}", vehicle);
        List<PartsRequestResponse> parts = partsRequestService.getPartsByVehicle(vehicle);
        return ResponseEntity.ok(parts);
    }
    
    /**
     * Get parts requests by service ID
     * GET /api/tracking/service/{serviceId}/parts-requests
     */
    @GetMapping("/service/{serviceId}/parts-requests")
    public ResponseEntity<List<PartsRequestResponse>> getPartsRequestsByService(@PathVariable String serviceId) {
        log.info("Fetching parts requests for service: {}", serviceId);
        List<PartsRequestResponse> parts = partsRequestService.getPartsByServiceId(serviceId);
        return ResponseEntity.ok(parts);
    }
}
