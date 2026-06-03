# Player Service

A Spring Boot REST API that serves historical baseball player data, with pagination, caching, request validation, and centralized error handling.

> **Using this as a machine-coding interview base?** Jump to [Interview Kickoff Checklist](#interview-kickoff-checklist).

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.3.4 |
| Persistence | Spring Data JPA + H2 (in-memory) |
| Caching | Caffeine (max 1000 entries, 10 min TTL) |
| Mapping | MapStruct 1.5.5 |
| Build | Maven |

## Prerequisites

- Java 17 — verify with `java -version`
- Maven — verify with `mvn --version`

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
| POST | `/v1/players` | Create a player (validated: `firstName`, `lastName` required) |
| PUT | `/v1/players/{id}` | Update a player (validated) |
| DELETE | `/v1/players/{id}` | Delete a player (`204`, or `404` if missing) |

Invalid input (missing required fields, malformed JSON, wrong param types) returns `400` with a `VALIDATION_ERROR` body. All errors share the `ErrorResponse` shape `{ code, message, timestamp }`.

### Example Requests

```shell
# Get first 10 players
curl "http://localhost:8080/v1/players?size=10"

# Get player by ID
curl "http://localhost:8080/v1/players/allenga01"

# Create a player (id is generated)
curl -X POST "http://localhost:8080/v1/players" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe"}'

# Update a player
curl -X PUT "http://localhost:8080/v1/players/{id}" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Doe"}'

# Delete a player
curl -X DELETE "http://localhost:8080/v1/players/{id}"
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
| `server.port` | `8080` | Application port |

## Running Tests

```shell
mvn test
```

## Interview Kickoff Checklist

This repo is a ready-to-go Spring Boot REST base. At the start of a machine-coding round, repurpose it to the problem domain (e.g. `Order`, `Vehicle`, `Booking`) in a few minutes:

**1. Rename the domain entity** — `src/main/java/.../model/Player.java`
- Swap the fields for your domain's (keep an `@Id`; `@GeneratedValue(strategy = UUID)` gives you free IDs).
- Update the matching DTOs: `CreatePlayerRequest`, `UpdatePlayerRequest`, `PlayerResponse`.
- Update `PlayerMapper` (MapStruct) to the new fields — it's annotation-driven, so usually just field renames.

**2. Fix the database seed** — `src/main/resources/schema.sql`
- It currently does `CREATE TABLE PLAYERS AS SELECT * FROM CSVREAD('Player.csv')`. **Delete that** unless you actually need seed data.
- Simplest path: drop `schema.sql`, delete `Player.csv`, and set `spring.jpa.hibernate.ddl-auto: create-drop` in `application.yml` so Hibernate generates the table from your `@Entity`.
- If you want seed rows, add a small `src/main/resources/data.sql` instead of the CSV.

**3. Rename endpoints & service** — `PlayerController` (`v1/players`), `PlayerService`, `PlayerRepository`.

**4. Adjust error codes** — `exception/ErrorCodes.java` and `ErrorCodeHttpStatusMapper.java` (rename `PLAYER_NOT_FOUND` etc. to your domain).

**5. Update/trim the tests** — the four test classes mirror the structure; rename builders and assertions to the new fields.

### What you get for free (don't rebuild these)
- **Layering:** controller → service → repository, with DTOs separate from the entity.
- **Validation:** add `@NotBlank` / `@NotNull` / `@Positive` to request DTOs; `@Valid` is already wired and `400 VALIDATION_ERROR` is handled globally.
- **Error handling:** `GlobalExceptionHandler` maps custom exceptions to status codes and turns bad input into `400` — extend `PlayerBaseException` for new error types.
- **Pagination:** both offset (`page`) and cursor (`after`) styles in `PlayerService.getPlayers`.
- **Caching:** Caffeine via `@Cacheable` / `@CacheEvict` on the service.
- **In-memory H2 + H2 console** for instant startup and live data inspection.

### Speed tips
- `mvn spring-boot:run` to start; `mvn test` to run the suite (seconds).
- Use the H2 console (`/h2-console`) to inspect data live during the demo.
- The `collection/*.http` files are runnable requests if your IDE supports them.
