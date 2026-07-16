# StayHub — Hotel Reservation System

A multi-module Spring Boot hotel reservation system. Covers full booking lifecycle with pessimistic-lock concurrency, JWT authentication, dynamic property search via Specification API, async Kafka events with a notification consumer, Redis caching, and Micrometer business metrics.

---

## Tech Stack

| Layer          | Technology                          |
|----------------|-------------------------------------|
| Language       | Java 21 (virtual threads enabled)   |
| Framework      | Spring Boot 3.3.6                   |
| Persistence    | Spring Data JPA / Hibernate 6       |
| Database       | PostgreSQL 15                       |
| Migrations     | Liquibase                           |
| Security       | Spring Security + JWT               |
| Messaging      | Apache Kafka                        |
| Caching        | Redis 7                             |
| Metrics        | Micrometer                          |
| Build          | Maven (multi-module)                |
| Infrastructure | Docker Compose                      |
| Testing        | JUnit 5, Mockito, AssertJ           |

---

## Architecture

```
stayhub-parent
├── stayhub-common          ← BaseEntity, DTOs, exceptions, GlobalExceptionHandler
├── stayhub-user            ← User entity, UserService, JWT SecurityConfig
├── stayhub-property        ← Property + Room entities, CRUD, Specification search
├── stayhub-booking         ← Booking lifecycle, pessimistic-lock concurrency, Micrometer counters
├── stayhub-notification    ← Kafka consumer for booking events
└── stayhub-api             ← Spring Boot entry point, CacheConfig, PropertySearchService, application.yml
```

Each business module is a plain Maven module with Spring beans — no `main` class.
`stayhub-api` is the only executable module. It scans `com.stayhub.*` to pick up all entities, repositories, and beans from every module.

---

## Modules

### stayhub-common
Shared infrastructure: `BaseEntity` (UUID PK, audit timestamps), `ApiResponse<T>` / `ErrorResponse` / `PageResponse<T>` records, domain exceptions (`ResourceNotFoundException`, `BookingConflictException`, `ValidationException`), and `GlobalExceptionHandler`.

### stayhub-user
`User` entity with `UserRole` (GUEST, HOST, ADMIN). `SecurityConfig` with JWT filter — all booking and property endpoints require a valid Bearer token.

### stayhub-property
`Property` and `Room` entities. `RoomStatus` (AVAILABLE, OCCUPIED, MAINTENANCE). `PropertySpecification` builds dynamic JPA `Specification` predicates for search by city, country, price range, and room type. `PropertyService.findById` is Redis-cached.

### stayhub-booking
`Booking` entity with `BookingStatusHistory` audit trail. `BookingService.createBooking` uses `@Lock(PESSIMISTIC_WRITE)` inside `@Transactional` to prevent double-booking. Micrometer counters track created, confirmed, and cancelled bookings.

### stayhub-notification
Kafka consumer (`@KafkaListener`) that handles `BookingEvent` messages published on every booking state change (PENDING / CONFIRMED / CANCELLED) and logs outbound notification intent.

### stayhub-api
`StayHubApplication` entry point. Owns `application.yml`, `CacheConfig` (Redis TTL, JSON serializer), `PropertySearchService` (orchestrates Specification + date availability across modules), and all Liquibase SQL migrations.

---

## Setup

### Prerequisites
- Java 21
- Maven 3.9+
- Docker + Docker Compose

### Start infrastructure

```bash
docker-compose up -d
```

Services started:
- PostgreSQL at `localhost:5432`
- Redis at `localhost:6379`
- Kafka + Zookeeper at `localhost:9092`
- pgAdmin UI at `http://localhost:5050` (credentials in `docker-compose.yml`)

Connect pgAdmin to the `stayhub-postgres` container:
- Host: `stayhub-postgres`, Port: `5432`, DB: `stayhub`

### Build all modules

```bash
./mvnw clean install -DskipTests
```

### Run the application

```bash
./mvnw spring-boot:run -pl stayhub-api
```

Liquibase runs automatically on startup and creates all tables.
Application available at `http://localhost:8080`.

### Health check

```
GET http://localhost:8080/actuator/health
```

### API documentation (Swagger UI)

```
http://localhost:8080/swagger-ui.html
```

---

## API Endpoints

> Endpoints return the resource DTO directly (`ResponseEntity<T>`), or `PageResponse<T>` for paginated lists. Errors are returned as `ErrorResponse` via `GlobalExceptionHandler`.

### Users — `/api/v1/users`

| Method | Path    | Description         |
|--------|---------|----------------------|
| POST   | `/`     | Register a new user |
| GET    | `/{id}` | Get user by ID      |
| PUT    | `/{id}` | Update user profile |

### Properties — `/api/v1/properties`

| Method | Path              | Description                                            |
|--------|-------------------|--------------------------------------------------------|
| GET    | `/`               | List all (paginated)                                   |
| GET    | `/{id}`           | Get property by ID (Redis-cached, TTL 10 min)          |
| GET    | `/search`         | Search with filters: city, country, minPrice, maxPrice, roomType, checkIn, checkOut |
| POST   | `/`               | Create property                                        |
| PUT    | `/{id}`           | Update property (evicts cache)                         |
| DELETE | `/{id}`           | Delete property (evicts cache)                         |

### Rooms — `/api/v1/properties/{propertyId}/rooms`

| Method | Path                            | Description              |
|--------|---------------------------------|--------------------------|
| GET    | `/`                             | List rooms for property  |
| GET    | `/available?checkIn=&checkOut=` | Find available rooms      |
| POST   | `/`                             | Add room to property      |
| PATCH  | `/{roomId}/status`              | Update room status        |

### Bookings — `/api/v1/bookings`

| Method | Path               | Description                  |
|--------|--------------------|------------------------------|
| POST   | `/`                | Create booking               |
| GET    | `/{id}`            | Get booking by ID            |
| GET    | `/guest/{guestId}` | List bookings for guest      |
| PUT    | `/{id}/confirm`    | Confirm a pending booking    |
| PUT    | `/{id}/cancel`     | Cancel a booking             |

