# FitQueue

A fitness class booking REST API built with Spring Boot.
Currently under development.

## Current features

- List fitness classes
- Find a class by ID
- Create a class
- Validate class title and capacity
- Return validation error messages
- Schedule classes with a required future start time
- Create members with name and email validation
- Reject duplicate member emails with HTTP 409
- Book classes with automatic waitlisting when full
- Reject duplicate bookings and bookings for started classes
- Cancel bookings before the two-hour cancellation deadline
- Automatically promote the first waitlisted booking when a confirmed booking is cancelled
- Repeat cancellation requests without promoting another member

## Tech stack

- Java 21
- Spring Boot 4.1.2
- Maven

## Run locally

Requires JDK 21 and Docker Desktop running.
Maven is provided through the Maven Wrapper.

```bash
git clone https://github.com/Ntgks/fitqueue.git
cd fitqueue
```

Create a `.env` file in the project root:

```dotenv
DB_PASSWORD=fitqueue-local-dev-password
```

Start PostgreSQL:
```bash
docker compose up -d
```

The database is available at `localhost:5433`.


Start the application on Windows (PowerShell):

```powershell
$env:DB_PASSWORD = "fitqueue-local-dev-password"
.\mvnw.cmd spring-boot:run
```

On macOS / Linux:

```bash
export DB_PASSWORD="fitqueue-local-dev-password"
sh mvnw spring-boot:run
```

Use the same password in `.env` and the environment variable.
Docker Compose reads `.env`; Spring Boot uses the environment variable.

The API runs at http://localhost:8080.

## API endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/classes | List classes |
| GET | /api/classes/{id} | Find a class by ID |
| POST | /api/classes | Create a class |
| POST | /api/members | Create a member |
| POST | /api/classes/{classId}/bookings | Create a booking for a member |
| DELETE | /api/classes/{classId}/bookings/{memberId} | Cancel a member's booking |

## Storage

Classes are stored in PostgreSQL. Database schema changes are managed with Flyway.

## Tests

Requires Docker running for PostgreSQL integration tests.

On Windows:

```powershell
.\mvnw.cmd test
```

On macOS / Linux:

```bash
sh mvnw test
```

Unit tests cover booking rules, cancellation and waitlist promotion.
A PostgreSQL integration test checks that two concurrent bookings for one seat
produce one confirmed booking and one waitlisted booking.