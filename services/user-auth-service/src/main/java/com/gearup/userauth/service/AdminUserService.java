package com.gearup.userauth.service;

import com.gearup.userauth.dto.*;
import com.gearup.userauth.exception.ResourceNotFoundException;
import com.gearup.userauth.exception.UserAlreadyExistsException;
import com.gearup.userauth.exception.BusinessException;
import com.gearup.userauth.model.Role;
import com.gearup.userauth.model.User;
import com.gearup.userauth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserService.class);

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final AuditService auditService;
    private final UserService userService;
    private final OTPService otpService;

    public AdminUserService(UserRepository userRepository, RoleService roleService,
                           AuditService auditService, UserService userService, OTPService otpService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.auditService = auditService;
        this.userService = userService;
        this.otpService = otpService;
    }

    /**
     * Get all users with pagination, filtering, and search
     */
    @Transactional(readOnly = true)
    public PageResponse<AdminUserListResponse> getAllUsers(
            int page, int size, String search, String roleFilter, 
            String statusFilter, String sortBy, String sortDir) {
        
        // Create sort
        Sort.Direction direction = sortDir.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        // Map sortBy field to entity field
        String entitySortField = switch (sortBy) {
            case "name" -> "displayName";
            case "email" -> "email";
            case "createdAt" -> "createdAt";
            case "lastLoginAt" -> "lastLoginAt";
            default -> "createdAt";
        };
        
        Sort sort = Sort.by(direction, entitySortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get all users (we'll filter in memory for now - can optimize with Specifications later)
        Page<User> userPage = userRepository.findAll(pageable);
        
        // Apply filters
        List<User> filteredUsers = userPage.getContent().stream()
                .filter(user -> matchesSearch(user, search))
                .filter(user -> matchesRoleFilter(user, roleFilter))
                .filter(user -> matchesStatusFilter(user, statusFilter))
                .collect(Collectors.toList());
        
        // Convert to response
        List<AdminUserListResponse> responseList = AdminUserListResponse.fromUsers(filteredUsers);
        
        return PageResponse.of(responseList, page, size, userPage.getTotalElements());
    }

    private boolean matchesSearch(User user, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String searchLower = search.toLowerCase();
        String name = user.getDisplayName() != null ? user.getDisplayName() : 
                     (user.getFirstName() + " " + user.getLastName()).trim();
        return name.toLowerCase().contains(searchLower) || 
               user.getEmail().toLowerCase().contains(searchLower);
    }

    private boolean matchesRoleFilter(User user, String roleFilter) {
        if (roleFilter == null || roleFilter.isBlank()) {
            return true;
        }
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(roleFilter));
    }

    private boolean matchesStatusFilter(User user, String statusFilter) {
        if (statusFilter == null || statusFilter.isBlank()) {
            return true;
        }
        String userStatus = user.getAccountStatus() == User.AccountStatus.ACTIVE ? "Active" : "Deactivated";
        return userStatus.equalsIgnoreCase(statusFilter);
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userService.getUserById(userId);
        return UserResponse.fromUser(user);
    }

    /**
     * Create new employee/admin account with OTP for password setup
     */
    @Transactional
    public OTPResponse createEmployee(AdminCreateEmployeeRequest request, String creatorFirebaseUid) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("email", request.getEmail());
        }

        // Get role
        Role role = roleService.getRoleByName(request.getRole());

        // Create user without Firebase UID (will be created during password setup)
        User user = new User();
        user.setEmail(request.getEmail());
        user.setDisplayName(request.getName());
        
        // Split name into first and last name (simple split)
        String[] nameParts = request.getName().split(" ", 2);
        user.setFirstName(nameParts[0]);
        user.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setEmailVerified(false);
    // Use a modifiable set for Hibernate to manage relationships properly
    user.setRoles(new java.util.HashSet<>(java.util.Collections.singleton(role)));
        user.setCreatedBy(creatorFirebaseUid);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // Set password setup flags
        user.setIsPasswordSet(false);

        User savedUser = userRepository.save(user);

        // Ensure a Firebase user exists immediately so admin can see UID in Firebase Console
        try {
            com.google.firebase.auth.FirebaseAuth firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
            com.google.firebase.auth.UserRecord firebaseUser;
            try {
                // Try to find existing Firebase user by email
                firebaseUser = firebaseAuth.getUserByEmail(savedUser.getEmail());
            } catch (com.google.firebase.auth.FirebaseAuthException notFound) {
                firebaseUser = null;
            }

            if (firebaseUser == null) {
                // Create with a strong temporary password and keep disabled until password setup
                String tempPassword = generateTempPassword();
                com.google.firebase.auth.UserRecord.CreateRequest createReq = new com.google.firebase.auth.UserRecord.CreateRequest()
                        .setEmail(savedUser.getEmail())
                        .setDisplayName(savedUser.getDisplayName())
                        .setPassword(tempPassword)
                        .setEmailVerified(false)
                        .setDisabled(true);
                firebaseUser = firebaseAuth.createUser(createReq);
                logger.info("Created Firebase user for {} with UID {} (disabled until password setup)", savedUser.getEmail(), firebaseUser.getUid());
            }

            // Save UID on local user if not set
            if (savedUser.getFirebaseUid() == null || savedUser.getFirebaseUid().isBlank()) {
                savedUser.setFirebaseUid(firebaseUser.getUid());
                userRepository.save(savedUser);
            }
        } catch (Exception e) {
            // Non-fatal: continue flow even if Firebase creation fails, but log clearly
            logger.warn("Failed to ensure Firebase user for {}: {}", savedUser.getEmail(), e.getMessage());
        }
        
        // Generate OTP for password setup
        String otp = otpService.generateOtp();
        otpService.setOtpForUser(savedUser, otp);
        
        logger.info("Employee account created by admin: {} with OTP for password setup", savedUser.getEmail());

        // Log audit - temporarily commented out to avoid issues
        /*
        User creator = userService.getUserByFirebaseUid(creatorFirebaseUid);
        auditService.logUserAction(savedUser.getId(), "EMPLOYEE_CREATED", 
                "Employee account created by admin: " + creator.getEmail(), 
                null, userService.userToMap(savedUser));
        */

        // Return OTP response
        return OTPResponse.builder()
                .otp(otp)
                .email(savedUser.getEmail())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .message("Employee created successfully. Share this OTP with the user to set up their password.")
                .build();
    }

    private String generateTempPassword() {
        // 20-char random alphanumeric temp password
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+";
        StringBuilder sb = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 20; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Update user role and status
     */
    @Transactional
    public UserResponse updateUser(Long userId, AdminUpdateUserRequest request, String adminFirebaseUid) {
        User user = userService.getUserById(userId);
        
        Map<String, Object> oldValues = userService.userToMap(user);

        // Update email if provided
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            // Check if email already exists for another user
            if (!user.getEmail().equals(request.getEmail()) && 
                userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already in use by another user");
            }
            user.setEmail(request.getEmail());
        }

        // Update first name if provided
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }

        // Update last name if provided
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }

        // Update display name based on first and last name
        if ((request.getFirstName() != null && !request.getFirstName().isBlank()) || 
            (request.getLastName() != null && !request.getLastName().isBlank())) {
            String displayName = user.getFirstName() + " " + user.getLastName();
            user.setDisplayName(displayName.trim());
        }

        // Update role (case-insensitive) and guard null
        Role newRole = null;
        try {
            newRole = roleService.getRoleByName(request.getRole());
        } catch (ResourceNotFoundException rnfe) {
            throw new BusinessException("Role not found: " + request.getRole());
        }
        if (newRole == null) {
            throw new BusinessException("Role resolution failed for: " + request.getRole());
        }
        user.setRoles(new HashSet<>(Collections.singleton(newRole)));

        // Update status
        User.AccountStatus newStatus = request.getStatus().equalsIgnoreCase("Active") ? 
                User.AccountStatus.ACTIVE : User.AccountStatus.INACTIVE;
        user.setAccountStatus(newStatus);
        
        user.setUpdatedBy(adminFirebaseUid);
        user.setUpdatedAt(LocalDateTime.now());

        // Ensure names are not blank to satisfy NOT NULL constraints
        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            user.setFirstName("User");
        }
        if (user.getLastName() == null) {
            user.setLastName("");
        }
        logger.debug("Persisting user update id={} email={} role={} status={}", user.getId(), user.getEmail(), newRole.getName(), user.getAccountStatus());
        User updatedUser = userRepository.save(user);
        logger.info("User {} updated by admin: role={}, status={}", userId, request.getRole(), request.getStatus());

        // Log audit
        User admin = userService.getUserByFirebaseUid(adminFirebaseUid);
        auditService.logUserAction(updatedUser.getId(), "USER_UPDATED", 
                "User updated by admin: " + admin.getEmail(), 
                oldValues, userService.userToMap(updatedUser));

        return UserResponse.fromUser(updatedUser);
    }

    /**
     * Update user's name using a single full-name field. Splits into first/last.
     * Rules:
     * - If one token => firstName=token, lastName="".
     * - If multiple tokens => firstName=first token, lastName=rest joined with spaces.
     */
    @Transactional
    public UserResponse updateUserName(Long userId, AdminUpdateUserNameRequest request, String adminFirebaseUid) {
        User user = userService.getUserById(userId);

        String trimmed = request.getName() != null ? request.getName().trim() : "";
        if (trimmed.isEmpty()) {
            throw new BusinessException("Name cannot be blank");
        }
        String[] parts = trimmed.split("\\s+");
        String first = parts[0];
        String last = parts.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length)) : "";

        Map<String, Object> oldValues = userService.userToMap(user);
        user.setFirstName(first);
        user.setLastName(last);
        user.setDisplayName(null); // ensure list response uses normalized first/last
        user.setUpdatedBy(adminFirebaseUid);
        user.setUpdatedAt(java.time.LocalDateTime.now());

        User saved = userRepository.save(user);

        // audit
        User admin = userService.getUserByFirebaseUid(adminFirebaseUid);
        auditService.logUserAction(saved.getId(), "USER_NAME_UPDATED",
                "User name updated by admin: " + admin.getEmail(),
                oldValues, userService.userToMap(saved));

        return UserResponse.fromUser(saved);
    }

    /**
     * Deactivate user account
     */
    @Transactional
    public UserResponse deactivateUser(Long userId, String adminFirebaseUid) {
        User user = userService.getUserById(userId);
        
        if (user.getAccountStatus() == User.AccountStatus.INACTIVE) {
            throw new BusinessException("User is already deactivated");
        }

        Map<String, Object> oldValues = userService.userToMap(user);
        
        user.setAccountStatus(User.AccountStatus.INACTIVE);
        user.setUpdatedBy(adminFirebaseUid);
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        logger.info("User {} deactivated by admin", userId);

        // Log audit
        User admin = userService.getUserByFirebaseUid(adminFirebaseUid);
        auditService.logUserAction(updatedUser.getId(), "USER_DEACTIVATED", 
                "User deactivated by admin: " + admin.getEmail(), 
                oldValues, userService.userToMap(updatedUser));

        return UserResponse.fromUser(updatedUser);
    }

    /**
     * Activate user account
     */
    @Transactional
    public UserResponse activateUser(Long userId, String adminFirebaseUid) {
        User user = userService.getUserById(userId);
        
        if (user.getAccountStatus() == User.AccountStatus.ACTIVE) {
            throw new BusinessException("User is already active");
        }

        Map<String, Object> oldValues = userService.userToMap(user);
        
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setUpdatedBy(adminFirebaseUid);
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        logger.info("User {} activated by admin", userId);

        // Log audit
        User admin = userService.getUserByFirebaseUid(adminFirebaseUid);
        auditService.logUserAction(updatedUser.getId(), "USER_ACTIVATED", 
                "User activated by admin: " + admin.getEmail(), 
                oldValues, userService.userToMap(updatedUser));

        return UserResponse.fromUser(updatedUser);
    }

    /**
     * Delete user account (soft delete)
     */
    @Transactional
    public void deleteUser(Long userId, String adminFirebaseUid) {
        User user = userService.getUserById(userId);
        
        Map<String, Object> oldValues = userService.userToMap(user);
        
        // Soft delete by deactivating
        user.setAccountStatus(User.AccountStatus.INACTIVE);
        user.setUpdatedBy(adminFirebaseUid);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        logger.info("User {} deleted (deactivated) by admin", userId);

        // Log audit
        User admin = userService.getUserByFirebaseUid(adminFirebaseUid);
        auditService.logUserAction(userId, "USER_DELETED", 
                "User deleted by admin: " + admin.getEmail(), 
                oldValues, null);
    }

    /**
     * Get user statistics
     */
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStatistics() {
        List<User> allUsers = userRepository.findAll();
        
        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream()
                .filter(u -> u.getAccountStatus() == User.AccountStatus.ACTIVE)
                .count();
        long deactivatedUsers = totalUsers - activeUsers;

        // Count by role
        long totalCustomers = countUsersByRole(allUsers, "CUSTOMER");
        long totalEmployees = countUsersByRole(allUsers, "EMPLOYEE");
        long totalAdmins = countUsersByRole(allUsers, "ADMIN");

        // New users this month
        LocalDateTime startOfMonth = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1);
        long newUsersThisMonth = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(startOfMonth))
                .count();

        // New users today
        LocalDateTime startOfToday = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        long newUsersToday = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(startOfToday))
                .count();

        // Role distribution
        Map<String, Long> usersByRole = allUsers.stream()
                .flatMap(u -> u.getRoles().stream())
                .collect(Collectors.groupingBy(Role::getName, Collectors.counting()));

        // Status distribution
        Map<String, Long> usersByStatus = allUsers.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getAccountStatus() == User.AccountStatus.ACTIVE ? "Active" : "Deactivated",
                        Collectors.counting()));

        return UserStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .deactivatedUsers(deactivatedUsers)
                .totalCustomers(totalCustomers)
                .totalEmployees(totalEmployees)
                .totalAdmins(totalAdmins)
                .newUsersThisMonth(newUsersThisMonth)
                .newUsersToday(newUsersToday)
                .usersByRole(usersByRole)
                .usersByStatus(usersByStatus)
                .build();
    }

    private long countUsersByRole(List<User> users, String roleName) {
        return users.stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals(roleName)))
                .count();
    }
}
