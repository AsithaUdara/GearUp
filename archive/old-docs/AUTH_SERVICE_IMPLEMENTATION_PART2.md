# Authentication & Authorization Service - Part 2: Implementation Files

## 📦 DTOs (Data Transfer Objects)

### RegisterUserRequest.java
```java
package com.gearup.userauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserRequest {
    
    @NotBlank(message = "Firebase UID is required")
    private String firebaseUid;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid")
    private String phoneNumber;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    private String displayName;
    private String profileImageUrl;
    private String role; // Default role to assign (e.g., "CUSTOMER")
}
```

### UserResponse.java
```java
package com.gearup.userauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String firebaseUid;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String displayName;
    private String profileImageUrl;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private String accountStatus;
    private Set<String> roles;
    private Set<String> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
```

### UpdateUserRequest.java
```java
package com.gearup.userauth.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid")
    private String phoneNumber;
    
    private String firstName;
    private String lastName;
    private String displayName;
    private String profileImageUrl;
}
```

### TokenResponse.java
```java
package com.gearup.userauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserResponse user;
}
```

### AuthRequest.java
```java
package com.gearup.userauth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    
    @NotBlank(message = "Firebase ID token is required")
    private String firebaseToken;
    
    private String deviceInfo;
    private String ipAddress;
}
```

### RoleRequest.java
```java
package com.gearup.userauth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {
    
    @NotBlank(message = "Role name is required")
    private String roleName;
}
```

---

## 🗄️ Repositories

### UserRepository.java
```java
package com.gearup.userauth.repository;

import com.gearup.userauth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByFirebaseUid(String firebaseUid);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    boolean existsByFirebaseUid(String firebaseUid);
    
    @Query("SELECT u FROM User u JOIN FETCH u.roles r JOIN FETCH r.permissions WHERE u.firebaseUid = :firebaseUid")
    Optional<User> findByFirebaseUidWithRolesAndPermissions(@Param("firebaseUid") String firebaseUid);
    
    @Query("SELECT u FROM User u WHERE u.accountStatus = 'ACTIVE'")
    java.util.List<User> findAllActiveUsers();
}
```

### RoleRepository.java
```java
package com.gearup.userauth.repository;

import com.gearup.userauth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT r FROM Role r JOIN FETCH r.permissions WHERE r.name = :name")
    Optional<Role> findByNameWithPermissions(String name);
}
```

### PermissionRepository.java
```java
package com.gearup.userauth.repository;

import com.gearup.userauth.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    Optional<Permission> findByName(String name);
    
    boolean existsByName(String name);
}
```

### UserSessionRepository.java
```java
package com.gearup.userauth.repository;

import com.gearup.userauth.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    
    Optional<UserSession> findBySessionToken(String sessionToken);
    
    Optional<UserSession> findByRefreshToken(String refreshToken);
    
    List<UserSession> findByUserIdAndIsActiveTrue(Long userId);
    
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.userId = :userId")
    void deactivateAllUserSessions(Long userId);
    
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :now")
    void deleteExpiredSessions(LocalDateTime now);
}
```

### UserAuditLogRepository.java
```java
package com.gearup.userauth.repository;

import com.gearup.userauth.model.UserAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserAuditLogRepository extends JpaRepository<UserAuditLog, Long> {
    
    List<UserAuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<UserAuditLog> findByActionAndCreatedAtBetween(String action, LocalDateTime start, LocalDateTime end);
}
```

---

## 🔧 Services

### UserService.java
```java
package com.gearup.userauth.service;

import com.gearup.userauth.dto.*;
import com.gearup.userauth.exception.ResourceNotFoundException;
import com.gearup.userauth.exception.UserAlreadyExistsException;
import com.gearup.userauth.model.Permission;
import com.gearup.userauth.model.Role;
import com.gearup.userauth.model.User;
import com.gearup.userauth.repository.RoleRepository;
import com.gearup.userauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditService auditService;
    private final EventPublisher eventPublisher;
    
    @Transactional
    public UserResponse registerUser(RegisterUserRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }
        
        if (userRepository.existsByFirebaseUid(request.getFirebaseUid())) {
            throw new UserAlreadyExistsException("User with Firebase UID already exists");
        }
        
        // Create new user
        User user = new User();
        user.setFirebaseUid(request.getFirebaseUid());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDisplayName(request.getDisplayName() != null ? request.getDisplayName() : 
            request.getFirstName() + " " + request.getLastName());
        user.setProfileImageUrl(request.getProfileImageUrl());
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        
        // Assign default role
        String roleName = request.getRole() != null ? request.getRole() : "CUSTOMER";
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        user.getRoles().add(role);
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Audit log
        auditService.logUserAction(savedUser.getId(), "USER_REGISTERED", "USER", 
            savedUser.getId().toString(), null, null);
        
        // Publish event
        eventPublisher.publishUserRegisteredEvent(savedUser);
        
        log.info("User registered successfully: {}", savedUser.getEmail());
        return mapToUserResponse(savedUser);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserByFirebaseUid(String firebaseUid) {
        User user = userRepository.findByFirebaseUidWithRolesAndPermissions(firebaseUid)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with Firebase UID: " + firebaseUid));
        return mapToUserResponse(user);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return mapToUserResponse(user);
    }
    
    @Transactional
    public UserResponse updateUser(String firebaseUid, UpdateUserRequest request) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getDisplayName() != null) user.setDisplayName(request.getDisplayName());
        if (request.getProfileImageUrl() != null) user.setProfileImageUrl(request.getProfileImageUrl());
        
        User updatedUser = userRepository.save(user);
        
        auditService.logUserAction(user.getId(), "USER_UPDATED", "USER", user.getId().toString(), null, null);
        eventPublisher.publishUserUpdatedEvent(updatedUser);
        
        return mapToUserResponse(updatedUser);
    }
    
    @Transactional
    public void updateLastLogin(String firebaseUid) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    @Transactional
    public void assignRole(String firebaseUid, String roleName) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        
        user.getRoles().add(role);
        userRepository.save(user);
        
        auditService.logUserAction(user.getId(), "ROLE_ASSIGNED", "USER_ROLE", 
            user.getId().toString(), null, roleName);
    }
    
    @Transactional
    public void removeRole(String firebaseUid, String roleName) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.getRoles().removeIf(role -> role.getName().equals(roleName));
        userRepository.save(user);
        
        auditService.logUserAction(user.getId(), "ROLE_REMOVED", "USER_ROLE", 
            user.getId().toString(), roleName, null);
    }
    
    private UserResponse mapToUserResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
        
        Set<String> permissions = user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(Permission::getName)
            .collect(Collectors.toSet());
        
        return UserResponse.builder()
            .id(user.getId())
            .firebaseUid(user.getFirebaseUid())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .displayName(user.getDisplayName())
            .profileImageUrl(user.getProfileImageUrl())
            .emailVerified(user.getEmailVerified())
            .phoneVerified(user.getPhoneVerified())
            .accountStatus(user.getAccountStatus().name())
            .roles(roleNames)
            .permissions(permissions)
            .createdAt(user.getCreatedAt())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }
}
```

### AuthService.java
```java
package com.gearup.userauth.service;

import com.gearup.userauth.dto.AuthRequest;
import com.gearup.userauth.dto.TokenResponse;
import com.gearup.userauth.dto.UserResponse;
import com.gearup.userauth.exception.AuthenticationException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserService userService;
    private final TokenService tokenService;
    
    @Transactional
    public TokenResponse authenticateWithFirebase(AuthRequest request) {
        try {
            // Verify Firebase token
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(request.getFirebaseToken());
            String firebaseUid = decodedToken.getUid();
            
            log.info("Firebase token verified for UID: {}", firebaseUid);
            
            // Get or create user
            UserResponse user = userService.getUserByFirebaseUid(firebaseUid);
            
            // Update last login
            userService.updateLastLogin(firebaseUid);
            
            // Generate internal tokens
            TokenResponse tokenResponse = tokenService.generateTokens(user, request);
            
            return tokenResponse;
            
        } catch (FirebaseAuthException e) {
            log.error("Firebase token verification failed", e);
            throw new AuthenticationException("Invalid Firebase token: " + e.getMessage());
        }
    }
    
    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        return tokenService.refreshAccessToken(refreshToken);
    }
    
    @Transactional
    public void logout(String sessionToken) {
        tokenService.invalidateSession(sessionToken);
    }
    
    public boolean validateToken(String token) {
        return tokenService.validateToken(token);
    }
}
```

I can continue with more service implementations. Would you like me to provide:

1. **TokenService** - JWT token generation and validation
2. **AuditService** - Audit logging
3. **EventPublisher** - Publishing events to other services
4. **Controllers** - REST API endpoints
5. **Security Configuration** - Spring Security setup
6. **Exception Handlers** - Global exception handling
7. **Tests** - Unit and integration tests

Let me know which parts you'd like me to continue with!
