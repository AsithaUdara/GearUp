# Chatbot Service Test Suite

This directory contains comprehensive tests for the chatbot service covering all layers of the application.

## Test Structure

```
src/test/java/com/gearup/chatbotservice/
├── controller/          # REST Controller tests
├── dto/                 # Data Transfer Object validation tests
├── entity/              # Entity model tests
├── integration/         # End-to-end integration tests
├── repository/          # Data access layer tests
├── service/             # Business logic layer tests
└── ChatbotServiceTestSuite.java  # Test suite runner
```

## Test Categories

### 1. Repository Tests (`@DataJpaTest`)

- **KnowledgeDocumentRepositoryTest**: Tests CRUD operations and vector similarity search
- **ChatSessionRepositoryTest**: Tests session management
- **ConversationHistoryRepositoryTest**: Tests conversation history storage

### 2. Service Tests (`@ExtendWith(MockitoExtension.class)`)

- **EmbeddingServiceTest**: Tests embedding generation with Ollama
- **RAGServiceTest**: Tests RAG (Retrieval Augmented Generation) functionality
- **ChatSessionServiceTest**: Tests session lifecycle management
- **IntentClassifierServiceTest**: Tests intent classification logic

### 3. Controller Tests (`@WebMvcTest`)

- **ChatControllerTest**: Tests REST API endpoints with MockMvc

### 4. Integration Tests (`@SpringBootTest`)

- **ChatbotIntegrationTest**: End-to-end tests with full application context

### 5. Entity Tests

- **KnowledgeDocumentTest**: Tests entity behavior and embedding conversion

### 6. DTO Tests

- **ChatMessageRequestTest**: Tests Bean Validation constraints

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=KnowledgeDocumentRepositoryTest
```

### Run Test Suite

```bash
mvn test -Dtest=ChatbotServiceTestSuite
```

### Run Tests by Category

```bash
# Repository tests only
mvn test -Dtest=*RepositoryTest

# Service tests only
mvn test -Dtest=*ServiceTest

# Controller tests only
mvn test -Dtest=*ControllerTest

# Integration tests only
mvn test -Dtest=*IntegrationTest
```

### Run with Coverage

```bash
mvn clean test jacoco:report
```

Coverage report will be available at `target/site/jacoco/index.html`

## Test Configuration

### Test Properties

Tests use `application-test.yml` with:

- H2 in-memory database (PostgreSQL compatibility mode)
- Flyway disabled (DDL auto-create)
- Eureka client disabled
- Mock Ollama service

### Test Database

- **Type**: H2 in-memory database
- **Mode**: PostgreSQL compatibility
- **DDL**: Auto-created from entities
- **Isolation**: Each test class gets a fresh database

## Key Testing Features

### 1. Mocking

- Uses Mockito for mocking dependencies
- WebClient mocking for external API calls
- Repository mocking in service tests

### 2. Assertions

- AssertJ for fluent assertions
- JUnit 5 assertions
- Custom matchers where needed

### 3. Test Data

- `@BeforeEach` setup methods for test data
- Builder pattern for complex objects
- Test fixtures in setUp methods

### 4. Transactions

- `@Transactional` for automatic rollback
- Clean database state between tests
- Isolation between test methods

## CI/CD Integration

These tests are designed to run in CI/CD pipelines:

### GitHub Actions Example

```yaml
- name: Run Tests
  run: mvn clean test

- name: Generate Test Report
  run: mvn surefire-report:report

- name: Upload Coverage
  run: mvn jacoco:report
```

### Test Execution Time

- Repository tests: ~2-5 seconds
- Service tests: ~1-3 seconds
- Controller tests: ~2-4 seconds
- Integration tests: ~5-10 seconds
- **Total**: ~10-20 seconds

## Best Practices

1. **Isolation**: Each test is independent
2. **Fast**: Uses in-memory database
3. **Deterministic**: No random behavior
4. **Readable**: Clear Given-When-Then structure
5. **Maintainable**: Well-organized and documented

## Test Coverage Goals

- **Line Coverage**: > 80%
- **Branch Coverage**: > 75%
- **Method Coverage**: > 85%

## Writing New Tests

When adding new features, follow these guidelines:

1. **Repository Layer**: Write `@DataJpaTest` for data access
2. **Service Layer**: Write unit tests with mocked dependencies
3. **Controller Layer**: Write `@WebMvcTest` for API endpoints
4. **Integration**: Add end-to-end tests for critical flows

### Example Test Template

```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {

    @Mock
    private MyRepository repository;

    @InjectMocks
    private MyService service;

    @Test
    void whenDoSomething_thenExpectedBehavior() {
        // Given
        // ... setup test data and mocks

        // When
        // ... execute the method under test

        // Then
        // ... verify the results
    }
}
```

## Troubleshooting

### H2 Database Issues

If you encounter H2 compatibility issues:

- Ensure H2 dependency is in test scope
- Check `MODE=PostgreSQL` in datasource URL
- Verify Flyway is disabled in test profile

### Mock Issues

If mocks aren't working:

- Verify `@ExtendWith(MockitoExtension.class)` is present
- Check mock initialization with `@Mock` and `@InjectMocks`
- Use `@MockBean` for Spring Boot tests

### WebClient Mocking

For WebClient tests:

```java
when(webClient.post()).thenReturn(requestBodyUriSpec);
when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
when(requestBodySpec.bodyValue(any())).thenReturn(requestBodySpec);
when(requestBodySpec.retrieve()).thenReturn(responseSpec);
```

## Additional Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
