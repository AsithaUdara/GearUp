package com.gearup.userauth.controller;

import com.gearup.userauth.dto.*;
import com.gearup.userauth.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Register a new user (public endpoint)
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(
            @Valid @RequestBody RegisterUserRequest request) {
        logger.info("User registration request received for email: {}", request.getEmail());
        UserResponse userResponse = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", userResponse));
    }

    /**
     * Get current user profile
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @RequestAttribute("firebaseUid") String firebaseUid) {
        logger.info("Get current user request for: {}", firebaseUid);
        UserResponse userResponse = userService.getUserResponseByFirebaseUid(firebaseUid);
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    /**
     * Update current user profile
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @RequestAttribute("firebaseUid") String firebaseUid,
            @Valid @RequestBody UpdateUserRequest request) {
        logger.info("Update user request for: {}", firebaseUid);
        UserResponse userResponse = userService.updateUser(firebaseUid, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", userResponse));
    }

    /**
     * Assign role to user (Admin only)
     */
    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> assignRole(
            @PathVariable Long userId,
            @Valid @RequestBody RoleAssignmentRequest request) {
        logger.info("Assign role request for user {}: {}", userId, request.getRoleName());
        UserResponse userResponse = userService.assignRole(userId, request.getRoleName());
        return ResponseEntity.ok(ApiResponse.success("Role assigned successfully", userResponse));
    }

    /**
     * Get user by ID (Admin only)
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long userId) {
        logger.info("Get user by ID request: {}", userId);
        UserResponse userResponse = UserResponse.fromUser(userService.getUserById(userId));
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }
}
