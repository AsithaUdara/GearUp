# Authentication & Authorization Service Implementation Guide

## 🎯 Overview

This guide will help you implement a complete Authentication & Authorization Service using Firebase as the auth provider. Your service will handle:

1. **User Management**: Registration, profile management, user CRUD
2. **Authentication**: Firebase token verification, session management
3. **Authorization**: Role-based access control (RBAC), permissions
4. **Token Management**: JWT generation, refresh tokens
5. **Event Publishing**: User lifecycle events for other services

---

## 📋 Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Prerequisites](#prerequisites)
3. [Step-by-Step Implementation](#step-by-step-implementation)
4. [Database Schema](#database-schema)
5. [API Endpoints](#api-endpoints)
6. [Security Configuration](#security-configuration)
7. [Testing](#testing)
8. [Integration with Other Services](#integration-with-other-services)

---

## 🏗️ Architecture Overview

### Authentication Flow

```
┌─────────┐         ┌──────────────┐         ┌─────────────────┐
│ Client  │────1───>│ API Gateway  │────2───>│  User Auth Svc  │
│ (App)   │         │              │         │                 │
└─────────┘         └──────────────┘         └─────────────────┘
                                                      │
                                                      │ 3. Verify Token
                                                      ▼
                                              ┌──────────────┐
                                              │   Firebase   │
                                              │   Admin SDK  │
                                              └──────────────┘
                                                      │
                                                      │ 4. Get User Info
                                                      ▼
                                              ┌──────────────┐
                                              │  PostgreSQL  │
                                              │  (Users DB)  │
                                              └──────────────┘
```

### Key Components

1. **Firebase Client SDK** (Frontend): Handles user signup/login
2. **Firebase Admin SDK** (Backend): Verifies tokens, manages users
3. **User Auth Service**: Manages user profiles, roles, permissions
4. **PostgreSQL Database**: Stores user data, roles, permissions
5. **Event Bus**: Publishes user events to other services

---

## ✅ Prerequisites

### 1. Firebase Project Setup

**Already completed** ✅ (You have `security-lib` with Firebase configuration)

Files you need:
- `firebase-service-account.json` - Firebase Admin SDK credentials
- Set environment variable: `FIREBASE_CONFIG_PATH=/path/to/firebase-service-account.json`

### 2. Verify Existing Setup

Your project already has:
- ✅ `shared-libs/security-lib` with Firebase config
- ✅ `FirebaseAuthenticationFilter` for token verification
- ✅ `FirebaseConfig` for initialization

---

## 🚀 Step-by-Step Implementation

### Step 1: Update `user-auth-service` Dependencies

Update `services/user-auth-service/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.gearup</groupId>
        <artifactId>gearup-root</artifactId>
        <version>1.0.0</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>

    <artifactId>user-auth-service</artifactId>
    <version>1.0.0</version>
    <name>user-auth-service</name>
    <description>Authentication and Authorization Service with Firebase</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Spring Cloud -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Flyway for database migrations -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>

        <!-- Redis for session/cache -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <!-- Internal Libraries -->
        <dependency>
            <groupId>com.gearup</groupId>
            <artifactId>shared-security-lib</artifactId>
            <version>1.0.0</version>
        </dependency>

        <dependency>
            <groupId>com.gearup</groupId>
            <artifactId>common-dto</artifactId>
            <version>1.0.0</version>
        </dependency>

        <dependency>
            <groupId>com.gearup</groupId>
            <artifactId>common-utils</artifactId>
            <version>1.0.0</version>
        </dependency>

        <dependency>
            <groupId>com.gearup</groupId>
            <artifactId>event-models</artifactId>
            <version>1.0.0</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Flyway Maven Plugin -->
            <plugin>
                <groupId>org.flywaydb</groupId>
                <artifactId>flyway-maven-plugin</artifactId>
            </plugin>

            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

### Step 2: Update Configuration

Update `config-repo/user-auth-service.yml`:

```yaml
spring:
  application:
    name: user-auth-service
  
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/as_user_auth_service}
    username: ${SPRING_DATASOURCE_USERNAME:svc_user_auth_service}
    password: ${SPRING_DATASOURCE_PASSWORD:auth_svc_pass_2024}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 20000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        use_sql_comments: true
  
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    schemas: public
  
  redis:
    host: ${SPRING_REDIS_HOST:redis}
    port: ${SPRING_REDIS_PORT:6379}
    timeout: 60000ms
    jedis:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

server:
  port: 8082

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka}
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}

# Firebase Configuration
app:
  firebase-configuration-file: ${FIREBASE_CONFIG_PATH:classpath:firebase-service-account.json}

# JWT Configuration (for internal service-to-service communication)
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-change-this-in-production}
  expiration: 86400000 # 24 hours
  refresh-expiration: 604800000 # 7 days

# Security Configuration
security:
  public-endpoints:
    - /actuator/health
    - /actuator/info
    - /api/v1/auth/register
    - /api/v1/auth/login
    - /api/v1/auth/refresh
    - /api/v1/auth/verify-token

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always

logging:
  level:
    com.gearup: DEBUG
    org.springframework.security: DEBUG
```

### Step 3: Create Database Schema

Create `services/user-auth-service/src/main/resources/db/migration/V1__initial_schema.sql`:

```sql
-- Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    firebase_uid VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200),
    profile_image_url TEXT,
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    account_status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Roles Table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Permissions Table
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User Roles (Many-to-Many)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(100),
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Role Permissions (Many-to-Many)
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- User Sessions (for tracking active sessions)
CREATE TABLE user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_token VARCHAR(500) UNIQUE NOT NULL,
    refresh_token VARCHAR(500) UNIQUE,
    device_info TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Audit Log
CREATE TABLE user_audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id VARCHAR(100),
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes for performance
CREATE INDEX idx_users_firebase_uid ON users(firebase_uid);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_account_status ON users(account_status);
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_token ON user_sessions(session_token);
CREATE INDEX idx_user_sessions_expires_at ON user_sessions(expires_at);
CREATE INDEX idx_user_audit_log_user_id ON user_audit_log(user_id);
CREATE INDEX idx_user_audit_log_created_at ON user_audit_log(created_at);

-- Comments
COMMENT ON TABLE users IS 'Core user profile information';
COMMENT ON TABLE roles IS 'System roles for RBAC';
COMMENT ON TABLE permissions IS 'Granular permissions for resources';
COMMENT ON TABLE user_sessions IS 'Active user sessions for token management';
COMMENT ON TABLE user_audit_log IS 'Audit trail of user actions';
```

Create `services/user-auth-service/src/main/resources/db/migration/V2__seed_roles_permissions.sql`:

```sql
-- Insert Default Roles
INSERT INTO roles (name, description) VALUES
('ADMIN', 'System administrator with full access'),
('CUSTOMER', 'Regular customer using the platform'),
('MECHANIC', 'Service provider/mechanic'),
('FLEET_MANAGER', 'Manages fleet of vehicles'),
('SUPPORT', 'Customer support representative');

-- Insert Default Permissions

-- User Management Permissions
INSERT INTO permissions (name, resource, action, description) VALUES
('user:read', 'USER', 'READ', 'View user information'),
('user:create', 'USER', 'CREATE', 'Create new users'),
('user:update', 'USER', 'UPDATE', 'Update user information'),
('user:delete', 'USER', 'DELETE', 'Delete users'),

-- Vehicle Management Permissions
('vehicle:read', 'VEHICLE', 'READ', 'View vehicle information'),
('vehicle:create', 'VEHICLE', 'CREATE', 'Add new vehicles'),
('vehicle:update', 'VEHICLE', 'UPDATE', 'Update vehicle information'),
('vehicle:delete', 'VEHICLE', 'DELETE', 'Remove vehicles'),

-- Booking Permissions
('booking:read', 'BOOKING', 'READ', 'View bookings'),
('booking:create', 'BOOKING', 'CREATE', 'Create bookings'),
('booking:update', 'BOOKING', 'UPDATE', 'Modify bookings'),
('booking:delete', 'BOOKING', 'DELETE', 'Cancel bookings'),

-- Payment Permissions
('payment:read', 'PAYMENT', 'READ', 'View payment information'),
('payment:process', 'PAYMENT', 'PROCESS', 'Process payments'),
('payment:refund', 'PAYMENT', 'REFUND', 'Issue refunds'),

-- Analytics Permissions
('analytics:read', 'ANALYTICS', 'READ', 'View analytics and reports'),
('analytics:export', 'ANALYTICS', 'EXPORT', 'Export analytics data'),

-- System Permissions
('system:configure', 'SYSTEM', 'CONFIGURE', 'Configure system settings'),
('system:audit', 'SYSTEM', 'AUDIT', 'View audit logs');

-- Assign Permissions to Roles

-- ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN';

-- CUSTOMER permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'CUSTOMER'
AND p.name IN (
    'vehicle:read',
    'booking:read',
    'booking:create',
    'booking:update',
    'payment:read',
    'payment:process'
);

-- MECHANIC permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'MECHANIC'
AND p.name IN (
    'vehicle:read',
    'vehicle:update',
    'booking:read',
    'booking:update'
);

-- FLEET_MANAGER permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'FLEET_MANAGER'
AND p.name IN (
    'vehicle:read',
    'vehicle:create',
    'vehicle:update',
    'booking:read',
    'analytics:read'
);

-- SUPPORT permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'SUPPORT'
AND p.name IN (
    'user:read',
    'vehicle:read',
    'booking:read',
    'booking:update',
    'payment:read'
);
```

### Step 4: Create Domain Models (Entities)

Create directory structure:
```
services/user-auth-service/src/main/java/com/gearup/userauth/
├── model/
│   ├── User.java
│   ├── Role.java
│   ├── Permission.java
│   ├── UserSession.java
│   └── UserAuditLog.java
├── dto/
├── repository/
├── service/
├── controller/
├── config/
└── UserAuthServiceApplication.java
```

**User.java**:
```java
package com.gearup.userauth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "firebase_uid", nullable = false, unique = true)
    private String firebaseUid;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Column(name = "display_name")
    private String displayName;
    
    @Column(name = "profile_image_url")
    private String profileImageUrl;
    
    @Column(name = "email_verified")
    private Boolean emailVerified = false;
    
    @Column(name = "phone_verified")
    private Boolean phoneVerified = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    private AccountStatus accountStatus = AccountStatus.ACTIVE;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum AccountStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        DELETED
    }
}
```

**Role.java**:
```java
package com.gearup.userauth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    private String description;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**Permission.java**:
```java
package com.gearup.userauth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private String resource;
    
    @Column(nullable = false)
    private String action;
    
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

I'll continue with more files in the next response. Would you like me to continue with:
- DTOs (Data Transfer Objects)
- Repositories
- Services
- Controllers
- Security Configuration
- Event Publishing

Let me know if you want me to proceed with the complete implementation!
