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

## License

MIT