package com.gearup.controller;

import com.gearup.domain.PartsRequestStatus;
import com.gearup.dto.CreatePartsRequestDTO;
import com.gearup.dto.PartsRequestDTO;
import com.gearup.service.PartsRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/parts-requests")
@RequiredArgsConstructor
public class PartsRequestController {
    private final PartsRequestService partsRequestService;

    @PostMapping
    public ResponseEntity<PartsRequestDTO> createRequest(
            @Valid @RequestBody CreatePartsRequestDTO request,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(partsRequestService.createPartsRequest(request, userId));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<Page<PartsRequestDTO>> getUserRequests(
            @AuthenticationPrincipal UUID userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(partsRequestService.getUserRequests(userId, pageable));
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<Page<PartsRequestDTO>> getRequestsByStatus(
            @PathVariable(value = "status") PartsRequestStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(partsRequestService.getRequestsByStatus(status, pageable));
    }

    @PutMapping("/{requestId}/status")
    public ResponseEntity<PartsRequestDTO> updateRequestStatus(
            @PathVariable(value = "requestId") UUID requestId,
            @RequestParam(value = "status") PartsRequestStatus status,
            @AuthenticationPrincipal UUID approverId) {
        return ResponseEntity.ok(partsRequestService.updateRequestStatus(requestId, status, approverId));
    }
}