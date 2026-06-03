# Player Service

A Spring Boot REST API that serves historical baseball player data, with pagination, caching, and LLM-powered chat capabilities via [Ollama](https://github.com/ollama/ollama).

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.3.4 |
| Persistence | Spring Data JPA + H2 (in-memory) |
| Caching | Caffeine (max 1000 entries, 10 min TTL) |
| Mapping | MapStruct 1.5.5 |
| LLM | Ollama4J 1.1.7 (llama3.2 default) |
| Build | Maven |

## Prerequisites

- Java 17 — verify with `java -version`
- Maven — verify with `mvn --version`
- Docker (for Ollama LLM integration)

## Getting Started

### 1. Install dependencies

```shell
mvn clean install -DskipTests
```

### 2. Run the service

```shell
mvn spring-boot:run
```

The service starts on `http://localhost:8080`.

### 3. Verify

```shell
curl http://localhost:8080/v1/players?size=10
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/v1/players?size={n}&page={n}&after={id}` | List players with pagination |
| GET | `/v1/players/{id}` | Get player by ID |
| POST | `/v1/players` | Create a player |
| PUT | `/v1/players/{id}` | Update a player |

### Example Requests

```shell
# Get first 10 players
curl "http://localhost:8080/v1/players?size=10"

# Get player by ID
curl "http://localhost:8080/v1/players/allenga01"

# Create a player
curl -X POST "http://localhost:8080/v1/players" \
  -H "Content-Type: application/json" \
  -d '{"playerID":"test001","nameFirst":"John","nameLast":"Doe"}'

# Update a player
curl -X PUT "http://localhost:8080/v1/players/test001" \
  -H "Content-Type: application/json" \
  -d '{"nameFirst":"Jane"}'
```

## LLM Integration (Optional)

The service integrates with Ollama to support AI-powered chat features.

### Start Ollama

```shell
# Pull and run Ollama
docker pull ollama/ollama
docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama ollama/ollama

# Pull the default model
docker exec -it ollama ollama pull llama3.2
```

### Verify Ollama is running

```shell
curl http://localhost:11434/api/generate \
  -H "Content-Type: application/json" \
  -d '{"model": "llama3.2", "prompt": "Hello", "stream": false}'
```

## H2 Console

The in-memory H2 database console is available at `http://localhost:8080/h2-console` while the service is running.

- **JDBC URL:** `jdbc:h2:mem:playerdb`
- **Username:** `sa`
- **Password:** _(empty)_

## Configuration

Key properties in `src/main/resources/application.yml`:

| Property | Default | Description |
|----------|---------|-------------|
| `player.pageLimit` | `10` | Default page size |
| `chat.default-model` | `llama3.2` | Ollama model to use |
| `model.server-url` | `http://localhost:5000` | ML model server URL |
| `server.port` | `8080` | Application port |

## Running Tests

```shell
mvn test
```
