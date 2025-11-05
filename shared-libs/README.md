# Shared LibrariesShared Libraries — how to use shared code

Reusable components shared across all GearUp microservices to prevent code duplication and ensure consistency.This folder contains common libraries used across microservices (DTOs, security helpers, event models).

## LibrariesWhen to add code here

| Library | Purpose |- Put code here if more than one service needs to use it (avoid duplication).

|---------|---------|

| `security-lib` | Firebase authentication utilities |How to depend on a shared lib

| `common-dto` | Shared data transfer objects |

| `common-utils` | Utility classes and helpers |1. Add the shared module as a Maven dependency in the consuming service's `pom.xml`.

| `event-models` | Event-driven architecture models |2. Build the reactor from repo root so shared libs are compiled before services that use them:

## security-lib/```

.\mvnw.cmd -DskipTests package

**Firebase authentication and authorization utilities**```

### UsageNotes

```xml

<dependency>- Keep shared modules small and focused. Avoid adding large runtime-dependent code that forces services to share too many transitive dependencies.

    <groupId>com.gearup</groupId>
    <artifactId>shared-security-lib</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
@Autowired
private FirebaseAuthService firebaseAuth;

public void verifyUser(String token) {
    FirebaseToken decoded = firebaseAuth.verifyToken(token);
    String uid = decoded.getUid();
}
```

## common-dto/

**Shared data transfer objects for inter-service communication**

### Usage

```xml
<dependency>
    <groupId>com.gearup</groupId>
    <artifactId>shared-common-dto</artifactId>
    <version>1.0.0</version>
</dependency>
```

Add DTOs in: `src/main/java/com/gearup/common/dto/`

## common-utils/

**Utility classes and helpers**

### Usage

```xml
<dependency>
    <groupId>com.gearup</groupId>
    <artifactId>shared-common-utils</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
import com.gearup.common.utils.ValidationUtils;

if (ValidationUtils.isValidEmail(email)) {
    // Process
}
```

## event-models/

**Event definitions for event-driven architecture**

### Usage

```xml
<dependency>
    <groupId>com.gearup</groupId>
    <artifactId>shared-event-models</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
// Publish event
rabbitTemplate.convertAndSend("exchange", "routing.key", event);

// Consume event
@RabbitListener(queues = "queue-name")
public void handleEvent(UserRegisteredEvent event) {
    // Process
}
```

## Building

```powershell
# Build all shared libraries
.\mvnw.cmd clean install

# Build specific library
.\mvnw.cmd clean install -pl shared-libs/security-lib -am
```

## Best Practices

1. **Keep libraries focused**: Single responsibility per library
2. **Document all public APIs**: Use JavaDoc
3. **Version properly**: Semantic versioning (1.0.0, 1.1.0, 2.0.0)
4. **Minimize dependencies**: Only include what's necessary
5. **Test thoroughly**: Unit tests for all utilities

See root `README.md` for complete documentation.
