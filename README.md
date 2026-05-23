# Uber App Backend

Spring Boot backend for a ride-booking platform inspired by Uber. The project is built as a single deployable monolith with clear separation between API controllers, business services, persistence repositories, entities, DTOs, security, and replaceable business strategies.

The goal of this project is to demonstrate practical backend engineering: authentication, authorization, ride lifecycle management, payments, wallet transactions, ratings, spatial driver matching, global exception handling, API documentation, and automated tests.

## Tech Stack

- Java 21
- Spring Boot 3.3.1
- Spring Web
- Spring Security
- Spring Data JPA
- Hibernate Spatial
- PostgreSQL with PostGIS
- JWT using `jjwt`
- ModelMapper
- Bean Validation
- Spring Mail
- Springdoc OpenAPI / Swagger UI
- Spring Boot Actuator
- JUnit 5
- Mockito
- Testcontainers
- JaCoCo
- Maven Wrapper

## Main Features

- Rider signup
- JWT-based login
- Refresh-token flow using an HTTP-only cookie
- Role-based authorization for riders, drivers, and admins
- Admin driver onboarding
- Rider ride request creation with pickup and drop-off locations
- Driver matching using spatial queries
- Driver ride acceptance
- OTP-based ride start
- Ride cancellation
- Ride completion
- Cash and wallet payment strategies
- Wallet credit and debit transactions
- Rider-to-driver rating
- Driver-to-rider rating
- Global API response wrapper
- Global exception handling
- Swagger UI for API exploration
- Unit tests and Testcontainers-based integration test setup

## Project Structure

```text
src/main/java/com/nikhil/project/uber/uberApp
+-- advices        # Global API response and exception handling
+-- configs        # Spring beans, security config, mapper config
+-- controllers    # REST API endpoints
+-- dto            # Request and response DTOs
+-- entities       # JPA entities and enums
+-- exceptions     # Custom application exceptions
+-- repositories   # Spring Data JPA repositories
+-- security       # JWT service and authentication filter
+-- services       # Business service interfaces
+-- services/impl  # Business workflow implementations
+-- strategies     # Strategy interfaces and strategy managers
+-- utils          # Geometry/location utilities
```

This is a monolith because all business capabilities are deployed as one application. Internally, the code is separated by technical responsibility. A future improvement would be packaging by business capability, for example `auth`, `rides`, `drivers`, `payments`, `wallets`, and `ratings`.

## Domain Model

The main domain objects are:

- `User`: login identity with one or more roles
- `Rider`: rider profile linked to a user
- `Driver`: driver profile linked to a user, with availability and vehicle information
- `RideRequest`: request created by a rider before a driver accepts it
- `Ride`: confirmed ride after a driver accepts a ride request
- `Payment`: payment record for a ride
- `Wallet`: user wallet balance
- `WalletTransaction`: debit or credit record for wallet activity
- `Rating`: stores rider and driver rating information for a completed ride

## Core Ride Flow

1. A user signs up through `POST /auth/signup`.
2. The user logs in through `POST /auth/login` and receives a JWT access token.
3. An admin can onboard a user as a driver through `POST /auth/onBoardNewDriver/{userId}`.
4. A rider requests a ride through `POST /riders/requestRide`.
5. The system calculates fare and finds matching nearby drivers.
6. A driver accepts the ride request through `POST /drivers/acceptRide/{rideRequestId}`.
7. The ride is created with `CONFIRMED` status and an OTP.
8. The driver starts the ride through `POST /drivers/startRide/{rideId}` after OTP verification.
9. The driver ends the ride through `POST /drivers/endRide/{rideId}`.
10. Payment is processed using the selected payment method.
11. Rider and driver can rate each other.

## Authentication And Authorization

The application uses Spring Security with JWT.

- Access tokens are returned from login.
- Refresh tokens are stored in an HTTP-only cookie.
- API requests are authenticated by `JwtAuthFilter`.
- Users have roles such as `RIDER`, `DRIVER`, and `ADMIN`.
- Role checks are enforced with Spring Security method security.

Public routes are limited to authentication endpoints. Other routes require a valid JWT.

## Strategy Pattern Usage

The project uses the strategy pattern where business rules can change independently.

Fare calculation:

- Default fare calculation
- Surge pricing fare calculation

Driver matching:

- Nearest available drivers
- Highest-rated nearby drivers

Payment:

- Wallet payment
- Cash payment

The strategy managers choose the correct implementation at runtime based on the current context, such as rider rating, current time, or payment method.

## Database And Spatial Queries

The project uses PostgreSQL with PostGIS-compatible spatial columns through Hibernate Spatial.

Driver matching uses native SQL queries with spatial functions such as:

- `ST_DWithin` to find drivers within a radius
- `ST_Distance` to order drivers by distance from the pickup location

This demonstrates location-aware backend logic instead of only basic CRUD operations.

## API Documentation

Swagger UI is available when the application is running:

```text
http://localhost:8080/swagger-ui/index.html
```

## Configuration

Copy `.env.example` values into your local environment or IDE run configuration.

Required variables:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET_KEY`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`

Production configuration expects secrets from environment variables. Local development can use defaults where appropriate.

Configuration files:

- `application.properties`: default application config
- `application-dev.properties`: local development config
- `application-prod.properties`: production-oriented config

## Run Locally

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```bash
.\mvnw.cmd spring-boot:run
```

The application starts on port `8080` by default.

## Test

Linux/macOS:

```bash
./mvnw clean test
```

Windows:

```bash
.\mvnw.cmd clean test
```

The test suite includes unit tests for controllers, services, strategies, JWT handling, DTO validation, mapper configuration, utility logic, and global response/error handling.

Testcontainers-based integration tests require Docker. If Docker is not running, those tests may be skipped depending on the test configuration.

## Code Quality

The project includes JaCoCo configuration for coverage reporting and a coverage check during Maven verification.

```bash
.\mvnw.cmd clean verify
```

JaCoCo excludes simple DTOs, entities, exception classes, and application bootstrap/config classes so coverage focuses on business behavior.

## Why This Works As A Monolith

This project is intentionally kept as one deployable application because the domain is still small enough to keep in one codebase while demonstrating multiple real backend capabilities.

Advantages of this approach:

- Simple deployment
- Easier local development
- Direct transaction boundaries
- Less distributed-system complexity
- Clear path to split modules later if needed

The code still separates responsibilities so the project is not a single unstructured codebase.

## Known Limitations And Future Improvements

- Package by business capability instead of only technical layers.
- Add database migrations using Flyway or Liquibase.
- Replace local date/time surge logic with a configurable pricing policy.
- Improve production readiness with structured logging, tracing, and stronger monitoring.
- Add more end-to-end API tests with Docker/PostGIS.
- Implement or remove currently unused wallet withdrawal behavior.
- Add refresh-token persistence/revocation for stronger session control.

## Interview Summary

This project can be presented as a Spring Boot ride-booking backend built as a monolith with clean layers and strategy-based business rules. It demonstrates REST API design, Spring Security with JWT, role-based access control, JPA persistence, PostGIS spatial driver matching, transactional payment and wallet flows, ratings, global error handling, Swagger documentation, and automated testing.

Good discussion points for an interview:

- Why a monolith is reasonable for this project size
- How the ride lifecycle moves through different statuses
- How JWT authentication and role-based authorization are handled
- Why the strategy pattern is used for fare, matching, and payment logic
- How spatial driver matching works with PostGIS
- Where transactions are important
- What should be improved before production use
