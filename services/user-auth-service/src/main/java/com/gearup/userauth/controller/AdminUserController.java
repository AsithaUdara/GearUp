package com.gearup.userauth.controller;

import com.gearup.userauth.dto.*;
import com.gearup.userauth.service.AdminUserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * Get all users with pagination, filtering, and search
     * 
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param search Search term (searches in name, email)
     * @param role Filter by role (ADMIN, EMPLOYEE, CUSTOMER)
     * @param status Filter by status (Active, Deactivated)
     * @param sortBy Sort field (createdAt, email, name, lastLoginAt)
     * @param sortDir Sort direction (ASC, DESC)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminUserListResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        logger.info("Admin get all users - page: {}, size: {}, search: {}, role: {}, status: {}", 
                page, size, search, role, status);
        
        PageResponse<AdminUserListResponse> response = adminUserService.getAllUsers(
                page, size, search, role, status, sortBy, sortDir);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get user details by ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long userId) {
        logger.info("Admin get user by ID: {}", userId);
        UserResponse userResponse = adminUserService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    /**
     * Create new employee/admin account
     * This will create a user WITHOUT Firebase UID (to be linked when they first login)
     */
    @PostMapping("/employees")
    public ResponseEntity<ApiResponse<UserResponse>> createEmployee(
            @Valid @RequestBody AdminCreateEmployeeRequest request,
            @RequestAttribute("firebaseUid") String creatorFirebaseUid) {
        
        logger.info("Admin creating new employee: {} with role: {}", request.getEmail(), request.getRole());
        UserResponse userResponse = adminUserService.createEmployee(request, creatorFirebaseUid);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee account created successfully. User can sign up with this email.", userResponse));
    }

    /**
     * Update user role and status
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUpdateUserRequest request,
            @RequestAttribute("firebaseUid") String adminFirebaseUid) {
        
        logger.info("Admin updating user {}: role={}, status={}", userId, request.getRole(), request.getStatus());
        UserResponse userResponse = adminUserService.updateUser(userId, request, adminFirebaseUid);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", userResponse));
    }

    /**
     * Deactivate user account
     */
    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(
            @PathVariable Long userId,
            @RequestAttribute("firebaseUid") String adminFirebaseUid) {
        
        logger.info("Admin deactivating user: {}", userId);
        UserResponse userResponse = adminUserService.deactivateUser(userId, adminFirebaseUid);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", userResponse));
    }

    /**
     * Activate user account
     */
    @PatchMapping("/{userId}/activate")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(
            @PathVariable Long userId,
            @RequestAttribute("firebaseUid") String adminFirebaseUid) {
        
        logger.info("Admin activating user: {}", userId);
        UserResponse userResponse = adminUserService.activateUser(userId, adminFirebaseUid);
        return ResponseEntity.ok(ApiResponse.success("User activated successfully", userResponse));
    }

    /**
     * Delete user account (soft delete by deactivating)
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long userId,
            @RequestAttribute("firebaseUid") String adminFirebaseUid) {
        
        logger.info("Admin deleting user: {}", userId);
        adminUserService.deleteUser(userId, adminFirebaseUid);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    /**
     * Get user statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats() {
        logger.info("Admin getting user statistics");
        UserStatsResponse stats = adminUserService.getUserStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
