package com.gearup.userauth.controller;

import com.gearup.userauth.dto.AdminCreateEmployeeRequest;
import com.gearup.userauth.dto.ApiResponse;
import com.gearup.userauth.dto.OTPResponse;
import com.gearup.userauth.service.AdminUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Development-only endpoints to assist local E2E tests without obtaining a Firebase ID token.
 * Protected by a shared secret header and only active under the 'dev' profile.
 */
@RestController
@RequestMapping("/api/v1/dev")
@Profile("dev")
public class DevAdminController {

    private static final Logger logger = LoggerFactory.getLogger(DevAdminController.class);

    private final AdminUserService adminUserService;

    @Value("${dev.admin.key:}")
    private String devAdminKey;

    public DevAdminController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PostMapping("/admin/users/employees")
    public ResponseEntity<ApiResponse<OTPResponse>> devCreateEmployee(
            @RequestHeader(name = "X-Dev-Admin-Key", required = false) String headerKey,
            @RequestBody AdminCreateEmployeeRequest request) {

        if (devAdminKey == null || devAdminKey.isBlank()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("DEV admin key not configured on server"));
        }
        if (headerKey == null || !devAdminKey.equals(headerKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid X-Dev-Admin-Key"));
        }

        logger.info("[DEV] Creating employee without Firebase client login: {}", request.getEmail());
        OTPResponse otp = adminUserService.createEmployee(request, "dev-admin");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("[DEV] Employee created; use returned OTP to set password.", otp));
    }
}
