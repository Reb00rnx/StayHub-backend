# StayHub — Hotel Reservation System

A multi-module Spring Boot hotel reservation system built progressively across 4 stages.
Stage 1 covers core domain CRUD and transactional booking logic.
Stages 2–4 add async messaging, caching, and observability.

---

## Tech Stack

| Layer          | Technology                          |
|----------------|-------------------------------------|
| Language       | Java 21 (virtual threads enabled)   |
| Framework      | Spring Boot 3.3.6                   |
| Persistence    | Spring Data JPA / Hibernate 6       |
| Database       | PostgreSQL 15                       |
| Migrations     | Liquibase                           |
| Security       | Spring Security (JWT — Stage 2)     |
| Messaging      | Kafka (Stage 2)                     |
| Caching        | Redis (Stage 4)                     |
| Build          | Maven (multi-module)                |
| Infrastructure | Docker Compose                      |

---

## Architecture

```
stayhub-parent
├── stayhub-common          ← BaseEntity, DTOs, exceptions, GlobalExceptionHandler
├── stayhub-user            ← User entity, UserService, SecurityConfig
├── stayhub-property        ← Property + Room entities, CRUD services
├── stayhub-booking         ← Booking entity, concurrency-safe BookingService
├── stayhub-notification    ← Placeholder (Stage 2: Kafka consumers)
└── stayhub-api             ← Spring Boot entry point, application.yml, Liquibase
```

Each business module is a plain Maven module with Spring beans — no `main` class.
`stayhub-api` is the only executable module. It scans `com.stayhub.*` to pick up all entities, repositories, and beans from every module.

---

## Modules

### stayhub-common
Shared infrastructure: `BaseEntity` (UUID PK, audit timestamps), `ApiResponse<T>` / `ErrorResponse` / `PageResponse<T>` records, domain exceptions, and `GlobalExceptionHandler`.

### stayhub-user
`User` entity with `UserRole` (GUEST, HOST, ADMIN). `SecurityConfig` permits all requests in Stage 1 — JWT filter added in Stage 2.

### stayhub-property
`Property` and `Room` entities. `RoomStatus` (AVAILABLE, OCCUPIED, MAINTENANCE). Room is a sub-resource of Property.

### stayhub-booking
`Booking` entity with `BookingStatusHistory` audit trail. `BookingService.createBooking` uses `@Lock(PESSIMISTIC_WRITE)` inside `@Transactional` to prevent double-booking.

### stayhub-notification
Empty placeholder. Stage 2 will add Kafka consumers that send email/push notifications on booking events.

### stayhub-api
`StayHubApplication` entry point. Owns `application.yml` (datasource, Liquibase, virtual threads) and all Liquibase SQL migrations.

---

## Setup

### Prerequisites
- Java 21
- Maven 3.9+
- Docker + Docker Compose

### Start the database

```bash
docker-compose up -d
```

- PostgreSQL available at `localhost:5432`
- pgAdmin UI at `http://localhost:5050` (admin@stayhub.com / admin_password)

Connect pgAdmin to the `stayhub-postgres` container:
- Host: `stayhub-postgres`, Port: `5432`, DB: `stayhub`, User: `stayhub`

### Build all modules

```bash
./mvnw clean compile
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

---

## API Endpoints

> All endpoints return `ApiResponse<T>` wrapper with `success`, `data`, and `message` fields.

### Users — `/api/v1/users`

| Method | Path              | Description         | Status  |
|--------|-------------------|---------------------|---------|
| GET    | `/{id}`           | Get user by ID      | TODO    |
| PUT    | `/{id}`           | Update user profile | TODO    |

### Properties — `/api/v1/properties`

| Method | Path              | Description           | Status  |
|--------|-------------------|-----------------------|---------|
| GET    | `/`               | List all (paginated)  | TODO    |
| GET    | `/{id}`           | Get property by ID    | TODO    |
| POST   | `/`               | Create property       | TODO    |
| PUT    | `/{id}`           | Update property       | TODO    |
| DELETE | `/{id}`           | Delete property       | TODO    |

### Rooms — `/api/v1/properties/{propertyId}/rooms`

| Method | Path                          | Description               | Status  |
|--------|-------------------------------|---------------------------|---------|
| GET    | `/`                           | List rooms for property   | TODO    |
| GET    | `/available?checkIn=&checkOut=` | Find available rooms    | TODO    |
| POST   | `/`                           | Add room to property      | TODO    |
| PATCH  | `/{roomId}/status`            | Update room status        | TODO    |

### Bookings — `/api/v1/bookings`

| Method | Path                  | Description                | Status  |
|--------|-----------------------|----------------------------|---------|
| POST   | `/`                   | Create booking             | TODO    |
| GET    | `/{id}`               | Get booking by ID          | TODO    |
| GET    | `/guest/{guestId}`    | List bookings for guest    | TODO    |
| PUT    | `/{id}/confirm`       | Confirm a pending booking  | TODO    |
| PUT    | `/{id}/cancel`        | Cancel a booking           | TODO    |

---

## Stage Roadmap

| Stage | Focus                        | Key additions                                                                  |
|-------|------------------------------|--------------------------------------------------------------------------------|
| **1** | Core domain *(current)*      | Multi-module Maven, PostgreSQL, Liquibase, CRUD, pessimistic-lock booking      |
| **2** | Async & security             | JWT authentication, Kafka events, email notifications via stayhub-notification |
| **3** | Advanced queries & reporting | Specification/criteria queries, booking analytics, availability calendar       |
| **4** | Performance & observability  | Redis caching (room availability), Micrometer metrics, distributed tracing     |
