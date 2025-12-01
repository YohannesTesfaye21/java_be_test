# Event Streaming Service

A Spring Boot REST API for managing user events (VIEW, ADD_TO_CART, PURCHASE) with authentication and event tracking capabilities.

## Features

- **User Authentication**: Token-based authentication with secure password hashing
- **Event Management**: Create, query, and analyze user events
- **Batch Operations**: Create multiple events in a single request
- **Advanced Filtering**: Search events by user, type, category, product, and date range
- **In-Memory Caching**: Fast access to recent 500 events via queue
- **Event Analytics**: Get event summaries grouped by type
- **API Documentation**: Swagger/OpenAPI integration

## Tech Stack

- **Framework**: Spring Boot
- **Database**: H2 (in-memory, configurable to PostgreSQL)
- **ORM**: JPA/Hibernate
- **Security**: BCrypt password hashing, token-based auth
- **Documentation**: Swagger/OpenAPI 3.0

## Project Structure

```
src/main/java/com/example/practical_test/
├── config/
│   ├── SwaggerConfig.java          # Swagger/OpenAPI configuration
│   └── TokenInterceptor.java       # Token validation filter
├── controller/
│   ├── AuthController.java         # Authentication endpoints
│   └── EventController.java        # Event management endpoints
├── dto/
│   ├── EventRequest.java           # Event creation request DTO
│   ├── EventResponse.java          # Event response DTO
│   ├── EventSummaryResponse.java   # Event summary DTO
│   ├── LoginRequest.java           # Login request DTO
│   ├── LoginResponse.java          # Login response DTO
│   └── ErrorResponse.java         # Error response DTO
├── exception/
│   └── GlobalExceptionHandler.java # Centralized exception handling
├── model/
│   ├── AuthUser.java               # User entity
│   └── Event.java                  # Event entity
├── repository/
│   ├── AuthUserRepository.java     # User repository
│   ├── EventRepository.java        # Event repository interface
│   ├── EventRepositoryCustom.java  # Custom repository interface
│   └── EventRepositoryImpl.java    # Custom query implementation
└── service/
    ├── AuthService.java            # Authentication logic
    ├── EventService.java           # Event business logic
    └── TokenService.java           # Token generation/validation
```

## How It Works

### Authentication Flow

1. **Login**: User sends credentials to `/auth/login`
   - `AuthService` validates username and password (BCrypt)
   - `TokenService` generates a UUID token
   - Token is stored in-memory and returned to client

2. **Token Validation**: All protected endpoints require authentication
   - `TokenInterceptor` checks `Authorization: Bearer <token>` header
   - Validates token before allowing request to proceed
   - Public endpoints: `/auth/login`, Swagger UI, H2 console

### Event Management Flow

1. **Create Event**: `POST /events`
   - Validates event type (VIEW, ADD_TO_CART, PURCHASE)
   - Saves event to database
   - Adds event to in-memory queue (max 500 events)
   - Returns created event details

2. **Batch Create**: `POST /events/batch`
   - Accepts list of events
   - Processes each event sequentially

3. **Query Events**: `GET /events`
   - Supports filters: userId, eventType, category, productId, date range
   - Uses JPA Criteria API for dynamic query building
   - Returns filtered events from database

4. **Recent Events**: `GET /events/recent`
   - Returns latest 20 events from in-memory queue
   - Fast access without database query

5. **Event Summary**: `GET /events/summary`
   - Groups events by type from in-memory queue
   - Optional category filter
   - Returns count per event type

## API Endpoints

### Authentication

- `POST /auth/login` - User login (returns token)

### Events

- `POST /events` - Create single event
- `POST /events/batch` - Create multiple events
- `GET /events` - Search/filter events (supports query parameters)
- `GET /events/recent` - Get latest 20 events
- `GET /events/summary` - Get event summary by type

## Configuration

The application uses H2 in-memory database by default. To switch to PostgreSQL, update `application.properties`:

```properties
# Uncomment PostgreSQL configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/event_streaming_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

## Running the Application

1. Build the project:
```bash
mvn clean install
```

2. Run the application:
```bash
mvn spring-boot:run
```

3. Access Swagger UI:
```
http://localhost:8080/swagger-ui.html
```

4. Access H2 Console (if enabled):
```
http://localhost:8080/h2-console
```

## Usage Example

1. **Login** to get a token:
```bash
POST /auth/login
{
  "username": "user1",
  "password": "password123"
}
```

2. **Create an event** using the token:
```bash
POST /events
Authorization: Bearer <token>
{
  "userId": 1,
  "eventType": "VIEW",
  "productId": 100,
  "category": "electronics",
  "timestamp": "2024-01-15T10:30:00"
}
```

3. **Query events**:
```bash
GET /events?eventType=VIEW&category=electronics
Authorization: Bearer <token>
```

## Key Components

- **TokenInterceptor**: Validates authentication tokens for protected endpoints
- **EventService**: Manages event creation, querying, and in-memory queue
- **EventRepositoryImpl**: Implements dynamic filtering using JPA Criteria API
- **GlobalExceptionHandler**: Centralized error handling and validation
- **TokenService**: In-memory token storage and validation

## Notes

- Tokens are stored in-memory (not persistent across restarts)
- In-memory queue maintains last 500 events for fast access
- Event types must be: VIEW, ADD_TO_CART, or PURCHASE
- Passwords are hashed using BCrypt

