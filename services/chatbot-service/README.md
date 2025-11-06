# Chatbot Service

AI-powered chatbot service with RAG (Retrieval-Augmented Generation) capabilities for GearUp Auto Service platform.

## Features

- **RAG Pipeline**: Vector similarity search with pgvector
- **Local LLM**: Ollama integration (llama3.2:3b)
- **Real-time Chat**: WebSocket support with STOMP
- **Intent Classification**: Rule-based pattern matching
- **Action Handling**: Booking, status checks, modifications
- **Conversation History**: Full conversation tracking
- **Session Management**: User session lifecycle management

## Technology Stack

- **Spring Boot 3.5.7**: Main framework
- **PostgreSQL + pgvector**: Vector database for embeddings
- **Ollama**: Local LLM for generation
- **WebSocket**: Real-time communication
- **RabbitMQ**: Event-driven architecture
- **Spring Cloud**: Service discovery and configuration

## Prerequisites

1. **Ollama** must be installed and running:

```bash
# Install Ollama
curl https://ollama.ai/install.sh | sh

# Pull required models
ollama pull llama3.2:3b
ollama pull nomic-embed-text
```

2. **PostgreSQL with pgvector** extension:

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

## Configuration

Key configuration properties (in `config-repo/chatbot-service.yml`):

```yaml
ollama:
  base-url: http://localhost:11434
  chat-model: llama3.2:3b
  embedding-model: nomic-embed-text

chatbot:
  rag:
    enabled: true
    top-k: 3
```

## API Endpoints

### WebSocket

- **Connect**: `ws://localhost:8084/ws/chat`
- **Subscribe**: `/user/queue/messages`
- **Send**: `/app/chat.send`

### REST

- `POST /api/chat/send` - Send message
- `GET /api/chat/history/{sessionId}` - Get conversation history
- `GET /api/chat/sessions` - Get user sessions
- `POST /api/chat/sessions/{sessionId}/close` - Close session

## Building

```bash
# Build with Maven
./mvnw clean package

# Build Docker image
docker build -t gearup/chatbot-service:latest .
```

## Running

```bash
# Run locally
./mvnw spring-boot:run

# Run with Docker
docker-compose up chatbot-service
```

## Knowledge Base

The service auto-loads knowledge base on startup including:

- Service catalog (oil change, full service, etc.)
- Company policies (cancellation, payment)
- FAQs
- Business information

## Architecture

```
User -> WebSocket -> ChatbotService
                          ↓
                    Intent Classifier
                     /          \
              Action Handler    RAG Service
                                    ↓
                              Embedding -> pgvector
                                    ↓
                              Retrieval -> Context
                                    ↓
                              Ollama LLM -> Response
```

## Development

### Adding New Services to Knowledge Base

Edit `KnowledgeBaseLoader.java` and add new documents:

```java
services.add(new DocumentData(
    "Service description with pricing and details...",
    Map.of("type", "service", "service_id", "new_service"),
    "service_catalog",
    "category"
));
```

### Adding New Intents

Edit `IntentClassifierService.java` and add patterns:

```java
"new_intent", new Pattern[] {
    Pattern.compile(".*\\b(keywords)\\b.*", Pattern.CASE_INSENSITIVE)
}
```

## License

Copyright © 2025 GearUp
