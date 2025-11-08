# GearUp Backend - New Service Development Guide

A complete step-by-step guide for building a new microservice in the GearUp backend ecosystem.

---

## 📋 Overview

This guide walks you through creating a new microservice that integrates with the GearUp infrastructure: Config Server, Eureka Service Discovery, and API Gateway.

---

## 🏗️ Step 1: Project Structure Setup

### 1.1 Create Service Directory

```
services/
  └── your-service-name/
      ├── src/
      │   ├── main/
      │   │   ├── java/com/gearup/yourservice/
      │   │   └── resources/
      │   └── test/
      │       └── java/com/gearup/yourservice/
      ├── Dockerfile
      ├── pom.xml
      └── README.md
```

### 1.2 Create pom.xml

**Parent reference:**

```xml
<parent>
    <groupId>com.gearup</groupId>
    <artifactId>gearup-root</artifactId>
    <version>1.0.0</version>
    <relativePath>../../pom.xml</relativePath>
</parent>

<artifactId>your-service-name</artifactId>
<version>1.0.0</version>
<name>your-service-name</name>
```

**Required Dependencies:**

```xml
<!-- Core Spring Boot -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Spring Cloud - Config & Discovery -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

<!-- Database -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Flyway Migrations -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>

<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>

<!-- Redis for Caching -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- RabbitMQ for Messaging -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>

<!-- Shared Libraries -->
<dependency>
    <groupId>com.gearup</groupId>
    <artifactId>shared-common-dto</artifactId>
    <version>1.0.0</version>
</dependency>

<dependency>
    <groupId>com.gearup</groupId>
    <artifactId>shared-common-utils</artifactId>
    <version>1.0.0</version>
</dependency>

<dependency>
    <groupId>com.gearup</groupId>
    <artifactId>shared-security-lib</artifactId>
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
```

---

## 🎯 Step 2: Main Application Class

Create `YourServiceApplication.java`:

```java
package com.gearup.yourservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient  // ← CRITICAL: Registers with Eureka
@ComponentScan(basePackages = {
    "com.gearup.yourservice",
    "com.gearup.security"  // Include shared security lib
})
public class YourServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(YourServiceApplication.class, args);
    }
}
```

**Key Points:**

- `@EnableDiscoveryClient` enables Eureka registration
- `@ComponentScan` includes shared security components

---

## ⚙️ Step 3: Configuration Files

### 3.1 Local Config: `src/main/resources/application.yml`

```yaml
spring:
  application:
    name: your-service-name # ← Used for Eureka registration

  config:
    import: optional:configserver:${SPRING_CLOUD_CONFIG_URI:http://localhost:8888}

server:
  port: 8087 # Choose an available port
```

### 3.2 Config Server: `config-repo/your-service-name.yml`

```yaml
spring:
  application:
    name: your-service-name

  # Database Configuration
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/as_your_service}
    username: ${SPRING_DATASOURCE_USERNAME:svc_your_service}
    password: ${SPRING_DATASOURCE_PASSWORD:your_pass_2024}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 20000

  # JPA Configuration
  jpa:
    hibernate:
      ddl-auto: validate # ← Always use 'validate' with Flyway
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

  # Flyway Configuration
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration

  # Redis Configuration
  redis:
    host: ${SPRING_REDIS_HOST:redis}
    port: ${SPRING_REDIS_PORT:6379}
    timeout: 60000ms
    jedis:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

  # RabbitMQ Configuration
  rabbitmq:
    host: ${SPRING_RABBITMQ_HOST:rabbitmq}
    port: ${SPRING_RABBITMQ_PORT:5672}
    username: ${SPRING_RABBITMQ_USERNAME:guest}
    password: ${SPRING_RABBITMQ_PASSWORD:guest}
    listener:
      simple:
        retry:
          enabled: true
          initial-interval: 3000
          max-attempts: 3
          multiplier: 2.0

server:
  port: 8087

# Eureka Client Configuration
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://admin:password@localhost:8761/eureka}
    register-with-eureka: true # ← CRITICAL: Enable registration
    fetch-registry: true # ← CRITICAL: Discover other services
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}

# Actuator for Health Checks
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

# Logging
logging:
  level:
    com.gearup: DEBUG
    org.springframework: INFO
```

---

## 🗄️ Step 4: Database Setup

### 4.1 Create Migration: `src/main/resources/db/migration/V1__initial_schema.sql`

```sql
-- Create main table
CREATE TABLE IF NOT EXISTS your_entities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_your_entities_name ON your_entities(name);
CREATE INDEX idx_your_entities_status ON your_entities(status);

-- Add comments
COMMENT ON TABLE your_entities IS 'Main entities for your service';
```

### 4.2 Update Root Makefile

Add Flyway target:

```makefile
flyway-your-service:
	@echo "Running Flyway migrations for your-service..."
	./mvnw -pl services/your-service-name flyway:migrate \
		-Dflyway.url=$${FLYWAY_URL_YOUR:-jdbc:postgresql://localhost:5432/as_your_service} \
		-Dflyway.user=$${FLYWAY_USER_YOUR:-svc_your_service} \
		-Dflyway.password=$${FLYWAY_PASSWORD_YOUR:-your_pass_2024}

flyway-all: flyway-automobile flyway-notification flyway-user-auth flyway-template flyway-your-service
```

---

## 🏛️ Step 5: Implement Core Components

### 5.1 Entity Layer

```java
package com.gearup.yourservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "your_entities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YourEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private String status;

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

### 5.2 Repository Layer

```java
package com.gearup.yourservice.repository;

import com.gearup.yourservice.entity.YourEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface YourEntityRepository extends JpaRepository<YourEntity, Long> {
    List<YourEntity> findByStatus(String status);
    List<YourEntity> findByNameContaining(String name);
}
```

### 5.3 Service Layer

```java
package com.gearup.yourservice.service;

import com.gearup.yourservice.entity.YourEntity;
import com.gearup.yourservice.repository.YourEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class YourService {

    private final YourEntityRepository repository;

    @Transactional(readOnly = true)
    public List<YourEntity> findAll() {
        log.info("Fetching all entities");
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public YourEntity findById(Long id) {
        log.info("Fetching entity by id: {}", id);
        return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Entity not found: " + id));
    }

    @Transactional
    public YourEntity create(YourEntity entity) {
        log.info("Creating new entity: {}", entity.getName());
        return repository.save(entity);
    }

    @Transactional
    public YourEntity update(Long id, YourEntity entity) {
        log.info("Updating entity: {}", id);
        YourEntity existing = findById(id);
        existing.setName(entity.getName());
        existing.setDescription(entity.getDescription());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting entity: {}", id);
        repository.deleteById(id);
    }
}
```

### 5.4 Controller Layer

```java
package com.gearup.yourservice.controller;

import com.gearup.yourservice.entity.YourEntity;
import com.gearup.yourservice.service.YourService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/your-resource")
@RequiredArgsConstructor
@Slf4j
public class YourController {

    private final YourService service;

    @GetMapping
    public ResponseEntity<List<YourEntity>> getAll() {
        log.info("GET /api/v1/your-resource - Fetch all");
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<YourEntity> getById(@PathVariable Long id) {
        log.info("GET /api/v1/your-resource/{} - Fetch by ID", id);
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<YourEntity> create(@RequestBody YourEntity entity) {
        log.info("POST /api/v1/your-resource - Create new");
        return ResponseEntity.ok(service.create(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<YourEntity> update(@PathVariable Long id, @RequestBody YourEntity entity) {
        log.info("PUT /api/v1/your-resource/{} - Update", id);
        return ResponseEntity.ok(service.update(id, entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/v1/your-resource/{} - Delete", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 🧪 Step 6: Testing

### 6.1 Unit Test Example

```java
package com.gearup.yourservice.service;

import com.gearup.yourservice.entity.YourEntity;
import com.gearup.yourservice.repository.YourEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("YourService Unit Tests")
class YourServiceTest {

    @Mock
    private YourEntityRepository repository;

    @InjectMocks
    private YourService service;

    private YourEntity entity;

    @BeforeEach
    void setUp() {
        entity = YourEntity.builder()
            .id(1L)
            .name("Test Entity")
            .description("Test Description")
            .status("ACTIVE")
            .build();
    }

    @Test
    @DisplayName("Should find all entities")
    void testFindAll() {
        // Given
        when(repository.findAll()).thenReturn(Arrays.asList(entity));

        // When
        List<YourEntity> result = service.findAll();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Entity");
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should create new entity")
    void testCreate() {
        // Given
        when(repository.save(any(YourEntity.class))).thenReturn(entity);

        // When
        YourEntity result = service.create(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Entity");
        verify(repository, times(1)).save(any(YourEntity.class));
    }
}
```

### 6.2 Integration Test Example

```java
package com.gearup.yourservice.integration;

import com.gearup.yourservice.entity.YourEntity;
import com.gearup.yourservice.repository.YourEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("Integration Tests")
@Transactional
class YourServiceIntegrationTest {

    @Autowired
    private YourEntityRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Should save and retrieve entity")
    void testSaveAndRetrieve() {
        // Given
        YourEntity entity = YourEntity.builder()
            .name("Integration Test")
            .description("Test Description")
            .status("ACTIVE")
            .build();

        // When
        YourEntity saved = repository.save(entity);
        YourEntity retrieved = repository.findById(saved.getId()).orElse(null);

        // Then
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getName()).isEqualTo("Integration Test");
    }
}
```

### 6.3 Test Configuration

Create `src/test/resources/application-test.yml`:

```yaml
spring:
  cloud:
    config:
      enabled: false
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect

eureka:
  client:
    enabled: false
```

---

## 🐳 Step 7: Docker Configuration

### 7.1 Create Dockerfile

```dockerfile
# Multi-stage Dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy pre-built JAR
COPY services/your-service-name/target/*.jar app.jar

USER spring:spring

# Expose port
EXPOSE 8087

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8087/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
```

### 7.2 Add to docker-compose.yml

```yaml
your-service:
  build:
    context: ../../
    dockerfile: ./services/your-service-name/Dockerfile
  container_name: gearup-your-service
  ports:
    - "8087:8087"
  env_file:
    - ./.env
  environment:
    - SPRING_DATASOURCE_URL=${YOUR_SERVICE_DB_URL}
    - SPRING_DATASOURCE_USERNAME=${YOUR_SERVICE_DB_USER}
    - SPRING_DATASOURCE_PASSWORD=${YOUR_SERVICE_DB_PASSWORD}
    - SPRING_JPA_HIBERNATE_DDL_AUTO=validate
    - SPRING_CLOUD_CONFIG_URI=http://config-server:8888
    - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=${EUREKA_DEFAULT_ZONE}
    - SPRING_REDIS_HOST=${SPRING_REDIS_HOST}
    - SPRING_REDIS_PORT=${SPRING_REDIS_PORT}
    - SPRING_RABBITMQ_HOST=${SPRING_RABBITMQ_HOST}
    - SPRING_RABBITMQ_PORT=${SPRING_RABBITMQ_PORT}
    - SPRING_RABBITMQ_USERNAME=${SPRING_RABBITMQ_USERNAME}
    - SPRING_RABBITMQ_PASSWORD=${SPRING_RABBITMQ_PASSWORD}
  depends_on:
    service-discovery:
      condition: service_healthy
    config-server:
      condition: service_healthy
    db:
      condition: service_healthy
    redis:
      condition: service_started
    rabbitmq:
      condition: service_healthy
  restart: unless-stopped
  healthcheck:
    test: ["CMD", "wget", "-qO-", "http://localhost:8087/actuator/health"]
    interval: 30s
    timeout: 10s
    retries: 3
    start_period: 60s
```

**Key Points:**

- Service depends on Redis and RabbitMQ
- Environment variables configured for both services
- Health checks ensure dependencies are ready

---

## 🌐 Step 8: API Gateway Integration

### 8.1 Add Route to API Gateway

Edit `api-gateway/src/main/resources/application.yml`:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: your-service-route
          uri: lb://your-service-name # ← Must match spring.application.name
          predicates:
            - Path=/api/v1/your-resource/**
          filters:
            - name: StripPrefix
              args:
                parts: 0 # Don't strip prefix
```

**Key Points:**

- `uri: lb://your-service-name` uses Eureka for load balancing
- `lb://` prefix tells Gateway to use service discovery
- Path must match your controller's `@RequestMapping`

---

## 🚀 Step 9: Build and Deploy

### 9.1 Add Module to Root pom.xml

```xml
<modules>
    <module>services/automobile-service</module>
    <module>services/notification-service</module>
    <module>services/template-service</module>
    <module>services/user-auth-service</module>
    <module>services/chatbot-service</module>
    <module>services/your-service-name</module>  <!-- Add this -->
    <!-- ... other modules ... -->
</modules>
```

### 9.2 Build the Service

```powershell
# Build entire project
.\mvnw.cmd clean package -DskipTests

# Or build just your service
.\mvnw.cmd -pl services/your-service-name clean package -DskipTests
```

### 9.3 Run Locally (Development)

```powershell
# Start infrastructure services first
cd deployment/docker
docker-compose up -d service-discovery config-server db redis rabbitmq

# Run your service locally
cd ../../services/your-service-name
..\..\mvnw.cmd spring-boot:run
```

### 9.4 Run with Docker (Production-like)

```powershell
# Build and start all services
cd deployment/docker
docker-compose build your-service
docker-compose up -d your-service
```

---

## ✅ Step 10: Verify Service Health

### 10.1 Health Check Sequence

**1. Service Starts:**

```
[INFO] Starting YourServiceApplication
[INFO] No active profile set, falling back to default profiles: default
```

**2. Config Server Connection:**

```
[INFO] Fetching config from server at: http://config-server:8888
[INFO] Located environment: name=your-service-name, profiles=[default]
```

**3. Database Connection:**

```
[INFO] HikariPool-1 - Starting...
[INFO] HikariPool-1 - Start completed.
```

**4. Flyway Migrations:**

```
[INFO] Flyway: Migrating schema to version 1 - initial schema
[INFO] Successfully applied 1 migration
```

**5. Eureka Registration:**

```
[INFO] Discovery Client initialized at timestamp XXX
[INFO] Registering application YOUR-SERVICE-NAME with eureka
[INFO] Registered with Eureka: your-service-name:XXX
```

**6. Application Ready:**

```
[INFO] Started YourServiceApplication in X.XXX seconds
```

### 10.2 Manual Health Checks

```powershell
# Check actuator health
curl http://localhost:8087/actuator/health

# Check Eureka dashboard
Start http://localhost:8761

# Test your endpoint
curl http://localhost:8087/api/v1/your-resource

# Test through API Gateway
curl http://localhost:9090/api/v1/your-resource
```

---

## 📊 Step 11: Monitoring & Observability

### 11.1 Actuator Endpoints

Available at `/actuator`:

- `/actuator/health` - Health status
- `/actuator/info` - Service info
- `/actuator/metrics` - Performance metrics
- `/actuator/prometheus` - Prometheus metrics

### 11.2 Logging Best Practices

```java
@Slf4j
public class YourService {
    public void method() {
        log.debug("Detailed debug info");    // Development
        log.info("Important events");        // Production
        log.warn("Warning conditions");      // Issues
        log.error("Error occurred", ex);     // Failures
    }
}
```

---

## � Step 12: RabbitMQ Integration (Inter-Service Communication)

RabbitMQ enables asynchronous messaging between services for event-driven architecture.

### 12.1 RabbitMQ Configuration Class

Create `config/RabbitMQConfig.java`:

```java
package com.gearup.yourservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String YOUR_SERVICE_EXCHANGE = "your-service.exchange";
    public static final String SYSTEM_EXCHANGE = "system.exchange";

    // Queue names
    public static final String YOUR_SERVICE_QUEUE = "your-service.queue";
    public static final String YOUR_SERVICE_NOTIFICATION_QUEUE = "your-service.notification.queue";

    // Routing keys
    public static final String YOUR_EVENT_ROUTING_KEY = "your-service.event.created";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.send";

    /**
     * Message converter for JSON serialization
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    /**
     * Topic Exchange for your service events
     */
    @Bean
    public TopicExchange yourServiceExchange() {
        return new TopicExchange(YOUR_SERVICE_EXCHANGE);
    }

    /**
     * Queue for your service events
     */
    @Bean
    public Queue yourServiceQueue() {
        return QueueBuilder.durable(YOUR_SERVICE_QUEUE)
            .withArgument("x-dead-letter-exchange", "dlx.exchange")
            .build();
    }

    /**
     * Queue for sending notifications
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(YOUR_SERVICE_NOTIFICATION_QUEUE)
            .build();
    }

    /**
     * Binding: your-service.queue to your-service.exchange
     */
    @Bean
    public Binding yourServiceBinding(Queue yourServiceQueue, TopicExchange yourServiceExchange) {
        return BindingBuilder
            .bind(yourServiceQueue)
            .to(yourServiceExchange)
            .with(YOUR_EVENT_ROUTING_KEY);
    }

    /**
     * Binding to system exchange for notifications
     */
    @Bean
    public TopicExchange systemExchange() {
        return new TopicExchange(SYSTEM_EXCHANGE);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange systemExchange) {
        return BindingBuilder
            .bind(notificationQueue)
            .to(systemExchange)
            .with(NOTIFICATION_ROUTING_KEY);
    }
}
```

### 12.2 Event Publisher Service

Create `service/EventPublisher.java`:

```java
package com.gearup.yourservice.service;

import com.gearup.yourservice.config.RabbitMQConfig;
import com.gearup.yourservice.dto.YourEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publish event when entity is created
     */
    public void publishEntityCreatedEvent(YourEventDTO event) {
        try {
            log.info("Publishing entity created event: {}", event);
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.YOUR_SERVICE_EXCHANGE,
                RabbitMQConfig.YOUR_EVENT_ROUTING_KEY,
                event
            );
            log.info("Event published successfully");
        } catch (Exception e) {
            log.error("Failed to publish event: {}", event, e);
            // Handle error - could store in DB for retry
        }
    }

    /**
     * Send notification request to notification service
     */
    public void sendNotification(String userId, String message) {
        try {
            NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .message(message)
                .type("SYSTEM")
                .build();

            log.info("Sending notification request: {}", request);
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.SYSTEM_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                request
            );
        } catch (Exception e) {
            log.error("Failed to send notification", e);
        }
    }
}
```

### 12.3 Event Listener

Create `listener/EventListener.java`:

```java
package com.gearup.yourservice.listener;

import com.gearup.yourservice.config.RabbitMQConfig;
import com.gearup.yourservice.dto.ExternalEventDTO;
import com.gearup.yourservice.service.EventHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventListener {

    private final EventHandlerService eventHandlerService;

    /**
     * Listen to events from your service queue
     */
    @RabbitListener(queues = RabbitMQConfig.YOUR_SERVICE_QUEUE)
    public void handleYourServiceEvent(YourEventDTO event) {
        try {
            log.info("Received event: {}", event);
            eventHandlerService.processEvent(event);
            log.info("Event processed successfully");
        } catch (Exception e) {
            log.error("Error processing event: {}", event, e);
            throw e; // Will trigger retry mechanism
        }
    }

    /**
     * Listen to events from other services
     * Example: User service sends user.created event
     */
    @RabbitListener(queues = "user.events.queue")
    public void handleUserEvent(ExternalEventDTO event) {
        try {
            log.info("Received user event: {}", event);
            // Handle user-related events
            eventHandlerService.handleExternalEvent(event);
        } catch (Exception e) {
            log.error("Error handling user event: {}", event, e);
        }
    }
}
```

### 12.4 Event DTOs

Create `dto/YourEventDTO.java`:

```java
package com.gearup.yourservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YourEventDTO {
    private String eventId;
    private String eventType;
    private Long entityId;
    private String entityName;
    private LocalDateTime timestamp;
    private String userId;
    private Object payload;
}
```

### 12.5 Usage in Service Layer

Update your service to publish events:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class YourService {

    private final YourEntityRepository repository;
    private final EventPublisher eventPublisher;

    @Transactional
    public YourEntity create(YourEntity entity) {
        log.info("Creating new entity: {}", entity.getName());
        YourEntity saved = repository.save(entity);

        // Publish event after successful save
        YourEventDTO event = YourEventDTO.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType("ENTITY_CREATED")
            .entityId(saved.getId())
            .entityName(saved.getName())
            .timestamp(LocalDateTime.now())
            .userId("system")
            .build();

        eventPublisher.publishEntityCreatedEvent(event);

        // Send notification
        eventPublisher.sendNotification(
            "admin",
            "New entity created: " + saved.getName()
        );

        return saved;
    }
}
```

### 12.6 Testing RabbitMQ Integration

Create `test/java/com/gearup/yourservice/messaging/RabbitMQIntegrationTest.java`:

```java
package com.gearup.yourservice.messaging;

import com.gearup.yourservice.config.RabbitMQConfig;
import com.gearup.yourservice.dto.YourEventDTO;
import com.gearup.yourservice.service.EventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.rabbitmq.host=localhost",
    "spring.rabbitmq.port=5672"
})
class RabbitMQIntegrationTest {

    @Autowired
    private EventPublisher eventPublisher;

    @Test
    void testPublishEvent() {
        // Given
        YourEventDTO event = YourEventDTO.builder()
            .eventId("test-001")
            .eventType("TEST_EVENT")
            .entityId(1L)
            .build();

        // When & Then - should not throw exception
        eventPublisher.publishEntityCreatedEvent(event);
    }
}
```

---

## 🗄️ Step 13: Redis Integration (Caching)

Redis provides fast caching and session management capabilities.

### 13.1 Redis Configuration Class

Create `config/RedisConfig.java`:

```java
package com.gearup.yourservice.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Configure ObjectMapper for Redis serialization
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // Enable type information for polymorphic deserialization
        mapper.activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );

        return mapper;
    }

    /**
     * RedisTemplate for generic Redis operations
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Use JSON serializer for values
        GenericJackson2JsonRedisSerializer jsonSerializer =
            new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Cache Manager with custom configurations
     */
    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {

        GenericJackson2JsonRedisSerializer jsonSerializer =
            new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(new StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(jsonSerializer)
            );

        // Custom cache configurations
        RedisCacheConfiguration entitiesConfig = defaultConfig
            .entryTtl(Duration.ofHours(1));

        RedisCacheConfiguration shortLivedConfig = defaultConfig
            .entryTtl(Duration.ofMinutes(5));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withCacheConfiguration("entities", entitiesConfig)
            .withCacheConfiguration("shortLived", shortLivedConfig)
            .build();
    }
}
```

### 13.2 Caching Service Layer

Update your service with caching annotations:

```java
package com.gearup.yourservice.service;

import com.gearup.yourservice.entity.YourEntity;
import com.gearup.yourservice.repository.YourEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class YourCachedService {

    private final YourEntityRepository repository;

    /**
     * Get all entities with caching
     * Cache name: entities
     * Key: "all"
     */
    @Cacheable(value = "entities", key = "'all'")
    @Transactional(readOnly = true)
    public List<YourEntity> findAll() {
        log.info("Fetching all entities from database (cache miss)");
        return repository.findAll();
    }

    /**
     * Get entity by ID with caching
     * Cache name: entities
     * Key: entity ID
     */
    @Cacheable(value = "entities", key = "#id")
    @Transactional(readOnly = true)
    public YourEntity findById(Long id) {
        log.info("Fetching entity {} from database (cache miss)", id);
        return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Entity not found: " + id));
    }

    /**
     * Create entity and update cache
     */
    @CacheEvict(value = "entities", key = "'all'")
    @Transactional
    public YourEntity create(YourEntity entity) {
        log.info("Creating new entity (will evict 'all' cache)");
        return repository.save(entity);
    }

    /**
     * Update entity and refresh cache
     */
    @CachePut(value = "entities", key = "#id")
    @CacheEvict(value = "entities", key = "'all'")
    @Transactional
    public YourEntity update(Long id, YourEntity entity) {
        log.info("Updating entity {} (will update cache)", id);
        YourEntity existing = findById(id);
        existing.setName(entity.getName());
        existing.setDescription(entity.getDescription());
        return repository.save(existing);
    }

    /**
     * Delete entity and evict from cache
     */
    @CacheEvict(value = "entities", key = "#id")
    @Transactional
    public void delete(Long id) {
        log.info("Deleting entity {} (will evict from cache)", id);
        repository.deleteById(id);
    }

    /**
     * Clear all caches
     */
    @CacheEvict(value = "entities", allEntries = true)
    public void clearCache() {
        log.info("Clearing all entity caches");
    }
}
```

### 13.3 Redis Template Service (Manual Cache Management)

Create `service/RedisCacheService.java`:

```java
package com.gearup.yourservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Set value with expiration
     */
    public void setValue(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("Set cache: key={}, ttl={}", key, ttl);
        } catch (Exception e) {
            log.error("Error setting cache for key: {}", key, e);
        }
    }

    /**
     * Get value by key
     */
    public <T> T getValue(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return type.cast(value);
        } catch (Exception e) {
            log.error("Error getting cache for key: {}", key, e);
            return null;
        }
    }

    /**
     * Delete by key
     */
    public void deleteKey(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Deleted cache: key={}", key);
        } catch (Exception e) {
            log.error("Error deleting cache for key: {}", key, e);
        }
    }

    /**
     * Check if key exists
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Error checking key existence: {}", key, e);
            return false;
        }
    }

    /**
     * Set expiration for existing key
     */
    public void expire(String key, Duration ttl) {
        try {
            redisTemplate.expire(key, ttl.getSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error setting expiration for key: {}", key, e);
        }
    }

    /**
     * Get keys by pattern
     */
    public Set<String> getKeysByPattern(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            log.error("Error getting keys by pattern: {}", pattern, e);
            return Set.of();
        }
    }

    /**
     * Store in hash
     */
    public void setHash(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
            log.debug("Set hash: key={}, field={}", key, field);
        } catch (Exception e) {
            log.error("Error setting hash: key={}, field={}", key, field, e);
        }
    }

    /**
     * Get from hash
     */
    public Object getHash(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("Error getting hash: key={}, field={}", key, field, e);
            return null;
        }
    }
}
```

### 13.4 Session Management with Redis

Create `service/SessionService.java`:

```java
package com.gearup.yourservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final RedisCacheService cacheService;
    private static final String SESSION_PREFIX = "session:";
    private static final Duration SESSION_TTL = Duration.ofHours(24);

    /**
     * Create new session
     */
    public String createSession(String userId, Map<String, Object> data) {
        String sessionId = UUID.randomUUID().toString();
        String key = SESSION_PREFIX + sessionId;

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("userId", userId);
        sessionData.put("createdAt", System.currentTimeMillis());
        sessionData.putAll(data);

        cacheService.setValue(key, sessionData, SESSION_TTL);
        log.info("Created session: {} for user: {}", sessionId, userId);

        return sessionId;
    }

    /**
     * Get session data
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        return cacheService.getValue(key, Map.class);
    }

    /**
     * Update session
     */
    public void updateSession(String sessionId, Map<String, Object> data) {
        String key = SESSION_PREFIX + sessionId;
        Map<String, Object> session = getSession(sessionId);

        if (session != null) {
            session.putAll(data);
            cacheService.setValue(key, session, SESSION_TTL);
            log.info("Updated session: {}", sessionId);
        }
    }

    /**
     * Delete session
     */
    public void deleteSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        cacheService.deleteKey(key);
        log.info("Deleted session: {}", sessionId);
    }

    /**
     * Extend session TTL
     */
    public void extendSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        cacheService.expire(key, SESSION_TTL);
        log.debug("Extended session: {}", sessionId);
    }
}
```

### 13.5 Testing Redis Integration

Create `test/java/com/gearup/yourservice/cache/RedisIntegrationTest.java`:

```java
package com.gearup.yourservice.cache;

import com.gearup.yourservice.entity.YourEntity;
import com.gearup.yourservice.service.YourCachedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Redis Caching Tests")
class RedisIntegrationTest {

    @Autowired
    private YourCachedService service;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Clear caches before each test
        cacheManager.getCacheNames()
            .forEach(name -> cacheManager.getCache(name).clear());
    }

    @Test
    @DisplayName("Should cache entity on first fetch")
    void testCaching() {
        // First call - cache miss
        YourEntity entity1 = service.findById(1L);

        // Second call - cache hit
        YourEntity entity2 = service.findById(1L);

        // Both should return same data
        assertThat(entity1).isNotNull();
        assertThat(entity2).isNotNull();
        assertThat(entity1.getId()).isEqualTo(entity2.getId());
    }

    @Test
    @DisplayName("Should evict cache on update")
    void testCacheEviction() {
        // Fetch entity (cache it)
        YourEntity entity = service.findById(1L);

        // Update entity (should evict cache)
        entity.setName("Updated Name");
        service.update(1L, entity);

        // Next fetch should get updated data from DB
        YourEntity updated = service.findById(1L);
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }
}
```

### 13.6 Redis Health Monitoring

Add to your application configuration:

```yaml
# In config-repo/your-service-name.yml
management:
  health:
    redis:
      enabled: true
  endpoints:
    web:
      exposure:
        include: health,info,metrics,caches
```

Create admin endpoint to manage caches:

```java
package com.gearup.yourservice.controller;

import com.gearup.yourservice.service.YourCachedService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/admin/cache")
@RequiredArgsConstructor
public class CacheAdminController {

    private final CacheManager cacheManager;
    private final YourCachedService cachedService;

    @PostMapping("/clear")
    public ResponseEntity<String> clearAllCaches() {
        cacheManager.getCacheNames()
            .forEach(name -> Objects.requireNonNull(
                cacheManager.getCache(name)).clear());
        return ResponseEntity.ok("All caches cleared");
    }

    @PostMapping("/clear/{cacheName}")
    public ResponseEntity<String> clearCache(@PathVariable String cacheName) {
        Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
        return ResponseEntity.ok("Cache cleared: " + cacheName);
    }

    @GetMapping("/names")
    public ResponseEntity<?> getCacheNames() {
        return ResponseEntity.ok(cacheManager.getCacheNames());
    }
}
```

---

## �🔒 Step 14: Security Integration

### 12.1 Add Security Configuration

```java
package com.gearup.yourservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .anyRequest().authenticated()
            )
            .csrf().disable();
        return http.build();
    }
}
```

---

## 📝 Checklist: Minimal Requirements

### ✅ Must Have:

- [ ] pom.xml with correct parent and dependencies (including Redis & RabbitMQ)
- [ ] Main application class with `@EnableDiscoveryClient`
- [ ] application.yml with service name and port
- [ ] config-repo/your-service-name.yml with full config (Redis & RabbitMQ)
- [ ] At least one Entity, Repository, Service, Controller
- [ ] RabbitMQ configuration (exchanges, queues, bindings)
- [ ] Redis configuration with caching enabled
- [ ] Event publisher for inter-service communication
- [ ] Event listener for consuming messages
- [ ] Flyway migration V1\_\_initial_schema.sql
- [ ] Dockerfile with health check
- [ ] Unit tests for service layer
- [ ] Integration test for repository
- [ ] RabbitMQ and Redis integration tests
- [ ] Docker compose entry
- [ ] API Gateway route configuration

### ✅ Tests Must Pass:

- [ ] Unit tests run successfully
- [ ] Integration tests run successfully
- [ ] Maven build completes without errors
- [ ] Docker image builds successfully
- [ ] Health check returns UP status
- [ ] Eureka shows service as registered
- [ ] API Gateway can route to service
- [ ] Database migrations apply successfully

---

## 🔧 Common Issues & Solutions

### Issue 1: Eureka Registration Fails

**Symptom:** Service doesn't appear in Eureka dashboard

**Solution:**

```yaml
# Check application.yml
eureka:
  client:
    register-with-eureka: true # Must be true
    fetch-registry: true
    service-url:
      defaultZone: http://admin:password@localhost:8761/eureka
```

### Issue 2: Config Server Connection Fails

**Symptom:** `Could not locate PropertySource`

**Solution:**

- Ensure config-server is running
- Check config file name matches `spring.application.name`
- Verify config-server URI is correct

### Issue 3: Database Connection Fails

**Symptom:** `Unable to create initial connections`

**Solution:**

- Verify PostgreSQL is running
- Check database exists: `CREATE DATABASE as_your_service;`
- Verify credentials in config

### Issue 4: Health Check Failing

**Symptom:** Container restarts continuously

**Solution:**

- Increase `start-period` in Dockerfile HEALTHCHECK
- Check application logs for startup errors
- Verify all dependencies are available

### Issue 5: API Gateway 404

**Symptom:** Gateway returns 404 for service routes

**Solution:**

- Verify service is registered in Eureka
- Check route path matches controller mapping
- Ensure `uri: lb://service-name` matches application name

### Issue 6: RabbitMQ Connection Fails

**Symptom:** `java.net.ConnectException: Connection refused` for RabbitMQ

**Solution:**

- Verify RabbitMQ is running: `docker ps | grep rabbitmq`
- Check credentials in configuration
- Ensure port 5672 is accessible
- Review RabbitMQ logs: `docker logs rabbitmq`

### Issue 7: Redis Connection Fails

**Symptom:** `Unable to connect to Redis` or caching not working

**Solution:**

- Verify Redis is running: `docker ps | grep redis`
- Check Redis host and port configuration
- Test connection: `redis-cli -h localhost -p 6379 ping`
- Ensure Redis health check passes

### Issue 8: When to Use RabbitMQ vs REST?

**Use RabbitMQ (Async) when:**

- Operation doesn't need immediate response
- Event notification (user created, order placed)
- Background processing (email sending, report generation)
- Decoupling services (fire and forget)
- High throughput needed
- Example: Sending notifications, logging events

**Use REST (Sync) when:**

- Need immediate response
- Request-response pattern required
- Data retrieval operations
- Client needs to know if operation succeeded immediately
- Example: Get user details, validate payment

---

## 🎯 Success Criteria

Your service is successfully integrated when:

1. ✅ **Maven Build:** `mvn clean package` succeeds
2. ✅ **Docker Build:** Image builds without errors
3. ✅ **Container Health:** Health check returns `healthy`
4. ✅ **Eureka Registration:** Service appears in dashboard at http://localhost:8761
5. ✅ **Config Loaded:** Logs show "Located environment" from config server
6. ✅ **Database Connected:** Flyway migrations apply successfully
7. ✅ **Redis Connected:** Caching works, Redis health check passes
8. ✅ **RabbitMQ Connected:** Can publish and consume messages
9. ✅ **API Gateway:** Requests route correctly through gateway
10. ✅ **Tests Pass:** All unit and integration tests pass

---

## 📚 Quick Reference

### Common Commands

```powershell
# Build project
.\mvnw.cmd clean package -DskipTests

# Run service locally
.\mvnw.cmd -pl services/your-service-name spring-boot:run

# Run Flyway migrations
make flyway-your-service

# Build Docker image
docker-compose -f deployment/docker/docker-compose.yml build your-service

# Start service
docker-compose -f deployment/docker/docker-compose.yml up -d your-service

# View logs
docker logs -f gearup-your-service

# Check health
curl http://localhost:8087/actuator/health
```

### Port Assignments

- 8761 - Eureka Service Discovery
- 8888 - Config Server
- 9090 - API Gateway
- 8081 - Notification Service
- 8082 - User Auth Service
- 8085 - Automobile Service
- 8086 - Chatbot Service
- 8087 - **Your Service** (example)

### Important URLs

- Eureka: http://localhost:8761 (admin/password)
- API Gateway: http://localhost:9090
- Config Server: http://localhost:8888
- Your Service: http://localhost:8087

---

## 🎓 Learning Resources

- **Spring Cloud Config:** https://spring.io/projects/spring-cloud-config
- **Spring Cloud Netflix:** https://spring.io/projects/spring-cloud-netflix
- **Flyway Migrations:** https://flywaydb.org/documentation
- **Spring Boot Actuator:** https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html
- **RabbitMQ Documentation:** https://www.rabbitmq.com/documentation.html
- **Spring AMQP:** https://spring.io/projects/spring-amqp
- **Redis Documentation:** https://redis.io/documentation
- **Spring Data Redis:** https://spring.io/projects/spring-data-redis

---

## 🎯 Real-World Example: Order Service

Here's a practical example of how all components work together:

### Scenario: User Places an Order

**1. REST API Call (Synchronous):**

```java
// OrderController receives request
@PostMapping("/orders")
public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
    Order order = orderService.createOrder(request);
    return ResponseEntity.ok(order);
}
```

**2. Database Operations:**

```java
// Save to database
Order saved = orderRepository.save(order);
```

**3. Cache Management:**

```java
// Invalidate user's orders cache
@CacheEvict(value = "user-orders", key = "#userId")
public Order createOrder(OrderRequest request) {
    // ... create order
}
```

**4. Publish Events (Asynchronous):**

```java
// Notify other services via RabbitMQ
OrderCreatedEvent event = OrderCreatedEvent.builder()
    .orderId(saved.getId())
    .userId(saved.getUserId())
    .amount(saved.getAmount())
    .build();

eventPublisher.publishOrderCreated(event);
```

**5. Other Services React:**

**Notification Service listens:**

```java
@RabbitListener(queues = "order.events.queue")
public void handleOrderCreated(OrderCreatedEvent event) {
    // Send email/SMS to user
    notificationService.sendOrderConfirmation(event.getUserId());
}
```

**Billing Service listens:**

```java
@RabbitListener(queues = "billing.events.queue")
public void handleOrderCreated(OrderCreatedEvent event) {
    // Process payment
    billingService.processPayment(event);
}
```

**Inventory Service listens:**

```java
@RabbitListener(queues = "inventory.events.queue")
public void handleOrderCreated(OrderCreatedEvent event) {
    // Reserve items
    inventoryService.reserveItems(event.getOrderId());
}
```

**6. Cache Subsequent Reads:**

```java
@Cacheable(value = "orders", key = "#orderId")
public Order getOrder(Long orderId) {
    // First call hits DB, subsequent calls from Redis
    return orderRepository.findById(orderId);
}
```

### Communication Flow:

```
Frontend → API Gateway → Order Service (REST)
                            ↓ Save to DB
                            ↓ Cache in Redis
                            ↓ Publish Event (RabbitMQ)
                            ↓
                    ┌───────┼───────┐
                    ↓       ↓       ↓
            Notification  Billing  Inventory
             Service      Service  Service
             (Async)      (Async)  (Async)
```

**Benefits:**

- ✅ Fast response to user (order saved)
- ✅ Decoupled services (independent scaling)
- ✅ Fault tolerance (if notification fails, order still created)
- ✅ Quick reads (cached data)
- ✅ Event-driven architecture (loosely coupled)

---

**Last Updated:** November 7, 2025
**Version:** 2.0.0
**Maintained by:** GearUp Team
