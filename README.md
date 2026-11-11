# FitQueue

A fitness class booking REST API built with Spring Boot.
Currently under development.

## Current features

- List fitness classes
- Find a class by ID
- Create a class
- Validate class title and capacity
- Return validation error messages

## Tech stack

- Java 21
- Spring Boot 4.1.2
- Maven

## Run locally

Requires JDK 21. Maven is provided through the Maven Wrapper.

```bash
git clone https://github.com/Ntgks/fitqueue.git
cd fitqueue
```

On Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS / Linux:

```bash
sh mvnw spring-boot:run
```

The API runs at http://localhost:8080.

## API endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/classes | List classes |
| GET | /api/classes/{id} | Find a class by ID |
| POST | /api/classes | Create a class |

## Storage

Data is currently stored in memory and is reset when the application restarts.

## Planned features

- PostgreSQL persistence
- Class bookings
- Waiting list with automatic promotion