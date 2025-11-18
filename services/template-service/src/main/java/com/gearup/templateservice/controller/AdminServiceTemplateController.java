package com.gearup.templateservice.controller;

import com.gearup.templateservice.dto.ApiResponse;
import com.gearup.templateservice.dto.ServiceTemplateDto;
import com.gearup.templateservice.service.ServiceTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/service-templates")
@RequiredArgsConstructor
// Require ADMIN role specifically (propagated via X-User-Roles header -> ROLE_ADMIN)
@PreAuthorize("hasRole('ADMIN')")
public class AdminServiceTemplateController {

    private final ServiceTemplateService service;

    @GetMapping
    public ResponseEntity<ApiResponse<com.gearup.templateservice.dto.PageResult<ServiceTemplateDto>>> list(
            @RequestParam(value = "activeOnly", defaultValue = "false") boolean activeOnly,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        var pageable = org.springframework.data.domain.PageRequest.of(Math.max(0, page), Math.max(1, size), org.springframework.data.domain.Sort.by("createdAt").descending());
        var pageResult = service.findAll(activeOnly, pageable);

        var dtoPage = com.gearup.templateservice.dto.PageResult.<ServiceTemplateDto>builder()
                .items(pageResult.getContent())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .build();

        return ResponseEntity.ok(ApiResponse.success(dtoPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceTemplateDto>> get(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServiceTemplateDto>> create(
            @Valid @RequestBody ServiceTemplateDto dto,
            @RequestAttribute(value = "firebaseUid", required = false) String adminUid,
            @RequestHeader(value = "X-Forwarded-Uid", required = false) String forwardedUid) {
        String effectiveAdminUid = adminUid != null ? adminUid : forwardedUid;
        if (effectiveAdminUid == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "admin UID not provided");
        }
        ServiceTemplateDto created = service.create(dto, effectiveAdminUid);
        return ResponseEntity.ok(ApiResponse.success("Service template created", created));
    }

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<ServiceTemplateDto>> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody ServiceTemplateDto dto,
            @RequestAttribute(value = "firebaseUid", required = false) String adminUid,
            @RequestHeader(value = "X-Forwarded-Uid", required = false) String forwardedUid) {
        String effectiveAdminUid = adminUid != null ? adminUid : forwardedUid;
        if (effectiveAdminUid == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "admin UID not provided");
        }
        ServiceTemplateDto updated = service.update(id, dto, effectiveAdminUid);
        return ResponseEntity.ok(ApiResponse.success("Service template updated", updated));
    }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("id") Long id,
            @RequestAttribute(value = "firebaseUid", required = false) String adminUid,
            @RequestHeader(value = "X-Forwarded-Uid", required = false) String forwardedUid) {
        String effectiveAdminUid = adminUid != null ? adminUid : forwardedUid;
        if (effectiveAdminUid == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "admin UID not provided");
        }
        service.delete(id, effectiveAdminUid);
        return ResponseEntity.ok(ApiResponse.success("Service template deleted", null));
    }
}
