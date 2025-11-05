package com.gearup.userauth.service;

import com.gearup.userauth.dto.RegisterUserRequest;
import com.gearup.userauth.dto.UpdateUserRequest;
import com.gearup.userauth.dto.UserResponse;
import com.gearup.userauth.exception.ResourceNotFoundException;
import com.gearup.userauth.exception.UserAlreadyExistsException;
import com.gearup.userauth.model.Role;
import com.gearup.userauth.model.User;
import com.gearup.userauth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final AuditService auditService;
    private final EventPublisher eventPublisher;

    public UserService(UserRepository userRepository, RoleService roleService, 
                      AuditService auditService, EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public UserResponse registerUser(RegisterUserRequest request) {
        // Check if user already exists
        if (userRepository.existsByFirebaseUid(request.getFirebaseUid())) {
            throw new UserAlreadyExistsException("Firebase UID", request.getFirebaseUid());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("email", request.getEmail());
        }

        // Get default role
        Role defaultRole = roleService.getRoleByName(request.getRole());

        // Create user
        User user = new User();
        user.setFirebaseUid(request.getFirebaseUid());
        user.setEmail(request.getEmail());
        user.setDisplayName(request.getDisplayName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPhotoUrl(request.getPhotoUrl());
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setRoles(Collections.singleton(defaultRole));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully: {}", savedUser.getEmail());

        // Log audit
        auditService.logUserAction(savedUser.getId(), "REGISTER", "User registered", null, userToMap(savedUser));

        // Publish event
        eventPublisher.publishUserRegisteredEvent(savedUser);

        return UserResponse.fromUser(savedUser);
    }

    @Transactional(readOnly = true)
    public User getUserByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUidWithRolesAndPermissions(firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException("User", "firebaseUid", firebaseUid));
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findByIdWithRolesAndPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserResponseByFirebaseUid(String firebaseUid) {
        User user = getUserByFirebaseUid(firebaseUid);
        return UserResponse.fromUser(user);
    }

    @Transactional
    public UserResponse updateUser(String firebaseUid, UpdateUserRequest request) {
        User user = getUserByFirebaseUid(firebaseUid);
        
        Map<String, Object> oldValues = userToMap(user);

        // Update fields
        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getPhotoUrl() != null) {
            user.setPhotoUrl(request.getPhotoUrl());
        }
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        logger.info("User updated successfully: {}", updatedUser.getEmail());

        // Log audit
        auditService.logUserAction(updatedUser.getId(), "UPDATE", "User profile updated", 
                oldValues, userToMap(updatedUser));

        // Publish event
        eventPublisher.publishUserUpdatedEvent(updatedUser);

        return UserResponse.fromUser(updatedUser);
    }

    @Transactional
    public UserResponse assignRole(Long userId, String roleName) {
        User user = getUserById(userId);
        Role role = roleService.getRoleByName(roleName);

        Map<String, Object> oldValues = userToMap(user);

        user.getRoles().add(role);
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        logger.info("Role '{}' assigned to user: {}", roleName, updatedUser.getEmail());

        // Log audit
        auditService.logUserAction(updatedUser.getId(), "ROLE_ASSIGNED", 
                "Role assigned: " + roleName, oldValues, userToMap(updatedUser));

        return UserResponse.fromUser(updatedUser);
    }

    @Transactional
    public void updateUserEntity(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("email", user.getEmail());
        map.put("displayName", user.getDisplayName());
        map.put("phoneNumber", user.getPhoneNumber());
        map.put("accountStatus", user.getAccountStatus());
        return map;
    }
}
