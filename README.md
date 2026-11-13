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
- List class bookings in creation order, including their current status
- Email and password authentication with BCrypt password hashing
- MEMBER and ADMIN access rules
- Session-based authentication with CSRF protection
- View the authenticated member's bookings, newest first

## Tech stack

- Java 21
- Spring Boot 4.1.2
- Maven

## Architecture

```mermaid
flowchart TD
    Client["Browser / API client"] --> Security["Spring Security filters"]
    Security --> Controller["Controllers"]
    Controller --> Service["Services"]
    Service --> Repository["Spring Data JPA repositories"]
    Repository --> Database[("PostgreSQL")]
```

Controllers handle HTTP requests and identify the authenticated member.
Services apply booking rules and manage transactions.
Repositories read and write PostgreSQL data through JPA.

Booking creation and cancellation lock the same class row using
PESSIMISTIC_WRITE within a transaction. This serializes these operations
for a given class, protecting seat allocation and waitlist promotion.

Flyway migrations define and version the database schema.

## Run locally

### Option 1: Docker Compose

Requires Docker Desktop running. No local Java or Maven installation is needed.

```bash
git clone https://github.com/Ntgks/fitqueue.git
cd fitqueue
```

Create a `.env` file in the project root:

```dotenv
DB_PASSWORD=fitqueue-local-dev-password
```

Build and start the application and PostgreSQL:

```bash
docker compose up --build -d
```

Follow application startup logs:

```bash
docker compose logs -f api
```

The API is available at http://localhost:8080 once startup completes.
PostgreSQL is available at localhost:5433.

Stop the containers while retaining database data:

```bash
docker compose down
```

### Option 2: Run Java locally

Requires JDK 21 and Docker Desktop running.
Maven is provided through the Maven Wrapper.

Create the `.env` file described above, then start only PostgreSQL:

```bash
docker compose up -d db
```

If the API container is already running, stop it first to free port 8080:

```bash
docker compose stop api
```

On Windows (PowerShell):

```powershell
$env:DB_PASSWORD = "fitqueue-local-dev-password"
.\mvnw.cmd spring-boot:run
```

On macOS / Linux:

```bash
export DB_PASSWORD="fitqueue-local-dev-password"
sh mvnw spring-boot:run
```

Use the same database password in `.env` and the environment variable.
Docker Compose reads `.env`; local Spring Boot uses the environment variable.

Run only one backend instance on port 8080 at a time.

## API endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/classes | List classes |
| GET | /api/classes/{id} | Find a class by ID |
| POST | /api/classes | Create a class |
| POST | /api/members | Create a member |
| POST | /api/classes/{classId}/bookings | Book a class for the authenticated member |
| GET | /api/members/me | Get the authenticated member |
| GET | /api/csrf | Get a CSRF token for the current session |
| GET | /api/classes/{classId}/bookings | List bookings for a class |
| DELETE | /api/classes/{classId}/bookings/me | Cancel the authenticated member's booking |
| GET | /api/bookings/me | List the authenticated member's bookings |

## Storage

Classes are stored in PostgreSQL. Database schema changes are managed with Flyway.

## Tests

Running tests locally requires JDK 21 and Docker running.
PostgreSQL integration tests use Testcontainers.

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
A PostgreSQL integration test verifies that cancellation and automatic
waitlist promotion are persisted in the database.
Time-dependent booking rules are tested using a fixed Clock,
including the exact two-hour cancellation deadline.

## Authentication

Authentication uses email and password through Spring Security form login
with session cookies. Passwords are stored as BCrypt hashes.

Registration requires name, email and password.
New accounts receive the MEMBER role.

Members create and cancel their own bookings. Booking creation requires no
request body; the member is identified from the authenticated session.

Creating classes and listing all bookings for a class require ADMIN.

POST and DELETE requests require a CSRF token and the matching session cookie.
Obtain a new token after login.

## Demo walkthrough

See the [PowerShell demo guide](docs/demo.md) for registration, login,
class creation, booking and cancellation.