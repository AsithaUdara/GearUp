package com.gearup.userauth.controller;

import com.gearup.userauth.dto.ApiResponse;
import com.gearup.userauth.dto.RoleResponse;
import com.gearup.userauth.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Get all roles with permissions
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        logger.info("Get all roles request");
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    /**
     * Get role by name with permissions
     */
    @GetMapping("/{name}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleByName(@PathVariable String name) {
        logger.info("Get role by name request: {}", name);
        RoleResponse role = roleService.getRoleResponseByName(name);
        return ResponseEntity.ok(ApiResponse.success(role));
    }
}
