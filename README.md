# Apartment Booking

Full-stack apartment booking application built with Angular and Spring Boot, containerized with Docker.

## Features

- (TODO: uzupełnimy w miarę jak backend/frontend będą gotowe — np. wyszukiwanie mieszkań, rezerwacje, panel admina)

## Tech Stack

**Frontend:** Angular, TypeScript
**Backend:** Java, Spring Boot, Spring Data JPA, Spring Security
**Database:** PostgreSQL
**Infrastructure:** Docker, Docker Compose

## Architecture

See [docs/architecture.md](docs/architecture.md) for details.

## Running locally

1. Copy `.env.example` to `.env` and adjust values if needed.
2. Run:
```
    docker compose up --build
```
3. Services:
   - Frontend: http://localhost:4200
   - Backend API: http://localhost:8080
   - PostgreSQL: localhost:5432

## Admin account

On first startup, the backend automatically creates one admin account using the
`ADMIN_EMAIL` and `ADMIN_PASSWORD` values from `.env`. The password is hashed with
BCrypt before being stored — no plaintext credentials ever touch the database.

## Local backend development (IntelliJ)

Running the whole stack via Docker is the simplest option (see above). If you prefer
running the backend directly in IntelliJ for hot-reload/debugging:

1. Start only the database: `docker compose up -d postgres`
2. In Run/Debug Configurations, either:
   - enable the **EnvFile** plugin and point it to the project's `.env`, or
   - manually set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`,
     `SPRING_DATASOURCE_PASSWORD` (and `ADMIN_EMAIL`/`ADMIN_PASSWORD`) as
     Environment Variables, matching your `.env` values.

Note: `SPRING_DATASOURCE_URL` differs between the two setups — `localhost:5433` when
running the backend locally, vs `postgres:5432` (the Docker service name) when running
everything through Docker Compose.


## License

MIT