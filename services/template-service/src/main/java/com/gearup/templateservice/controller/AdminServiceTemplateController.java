package com.gearup.templateservice.controller;

import com.gearup.templateservice.dto.ApiResponse;
import com.gearup.templateservice.dto.ServiceTemplateDto;
import com.gearup.templateservice.service.ServiceTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/service-templates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminServiceTemplateController {

    private final ServiceTemplateService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceTemplateDto>>> list(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return ResponseEntity.ok(ApiResponse.success(service.findAll(activeOnly)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceTemplateDto>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServiceTemplateDto>> create(
            @Valid @RequestBody ServiceTemplateDto dto,
            @RequestAttribute(value = "firebaseUid", required = false) String adminUid) {
        ServiceTemplateDto created = service.create(dto, adminUid);
        return ResponseEntity.ok(ApiResponse.success("Service template created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceTemplateDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody ServiceTemplateDto dto,
            @RequestAttribute(value = "firebaseUid", required = false) String adminUid) {
        ServiceTemplateDto updated = service.update(id, dto, adminUid);
        return ResponseEntity.ok(ApiResponse.success("Service template updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @RequestAttribute(value = "firebaseUid", required = false) String adminUid) {
        service.delete(id, adminUid);
        return ResponseEntity.ok(ApiResponse.success("Service template deleted", null));
    }
}
