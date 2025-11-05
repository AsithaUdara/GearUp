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

    public AdminUserService(UserRepository userRepository, RoleService roleService,
                           AuditService auditService, UserService userService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.auditService = auditService;
        this.userService = userService;
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
     * Create new employee/admin account
     * Creates a "pending" account that will be activated when user signs up with Firebase
     */
    @Transactional
    public UserResponse createEmployee(AdminCreateEmployeeRequest request, String creatorFirebaseUid) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("email", request.getEmail());
        }

        // Get role
        Role role = roleService.getRoleByName(request.getRole());

        // Create user with temporary Firebase UID (will be updated on first login)
        User user = new User();
        user.setFirebaseUid("pending_" + UUID.randomUUID().toString()); // Temporary UID
        user.setEmail(request.getEmail());
        user.setDisplayName(request.getName());
        
        // Split name into first and last name (simple split)
        String[] nameParts = request.getName().split(" ", 2);
        user.setFirstName(nameParts[0]);
        user.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setRoles(Collections.singleton(role));
        user.setCreatedBy(creatorFirebaseUid);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        logger.info("Employee account created by admin: {}", savedUser.getEmail());

        // Log audit
        User creator = userService.getUserByFirebaseUid(creatorFirebaseUid);
        auditService.logUserAction(savedUser.getId(), "EMPLOYEE_CREATED", 
                "Employee account created by admin: " + creator.getEmail(), 
                null, userService.userToMap(savedUser));

        return UserResponse.fromUser(savedUser);
    }

    /**
     * Update user role and status
     */
    @Transactional
    public UserResponse updateUser(Long userId, AdminUpdateUserRequest request, String adminFirebaseUid) {
        User user = userService.getUserById(userId);
        
        Map<String, Object> oldValues = userService.userToMap(user);

        // Update role
        Role newRole = roleService.getRoleByName(request.getRole());
        user.setRoles(Collections.singleton(newRole));

        // Update status
        User.AccountStatus newStatus = request.getStatus().equalsIgnoreCase("Active") ? 
                User.AccountStatus.ACTIVE : User.AccountStatus.INACTIVE;
        user.setAccountStatus(newStatus);
        
        user.setUpdatedBy(adminFirebaseUid);
        user.setUpdatedAt(LocalDateTime.now());

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
