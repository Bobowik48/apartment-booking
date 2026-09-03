# Apartment Booking

Full-stack apartment booking application built with Angular and Spring Boot, containerized with Docker.

## Features

- Public apartment listing with photo gallery and an availability calendar
- Booking flow for both guests (no account required) and logged-in users, with guest access via a private tokenized link
- PayU payment integration (sandbox by default), with automatic payment status confirmation via webhook
- JWT-based authentication — register, login, change password, forgot/reset password with emailed reset links
- Admin panel for managing apartments, photos, blocked dates and reservations
- Automatic expiration of unpaid reservations after a configurable time window
- Transactional emails via Brevo (booking confirmation, password reset)
- Swagger / OpenAPI documentation for the whole API

## Tech Stack

**Frontend:** Angular, TypeScript
**Backend:** Java, Spring Boot, Spring Data JPA, Spring Security
**Database:** PostgreSQL
**Infrastructure:** Docker, Docker Compose

## Architecture

See [docs/architecture.md](docs/architecture.md) for the full picture — request flow, frontend/backend design choices, and how the PayU + ngrok + email flows fit together.

## Database schema

See [docs/database.md](docs/database.md).

## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (includes Docker Compose)
- Git

That's genuinely all you need to get the app running. Java, Maven and Node are only required if you want to run the backend or frontend outside Docker — see "Local backend development" below.

## Getting started (Docker — recommended)

1. Clone the repository and enter it:
   ```
   git clone https://github.com/Bobowik48/apartment-booking.git
   cd apartment-booking
   ```

2. Create your own environment file from the template:
   ```
   cp .env.example .env
   ```
   (On Windows without `cp` available, just copy `.env.example` in File Explorer, paste it in the same folder, and rename the copy to `.env`.)

   `.env` is git-ignored and stays on your machine only — `.env.example` is the committed template, it is never read by the app itself.

3. Open `.env` and fill in the values. Every variable is explained inline in `.env.example`; in short:
   - Several already have safe, working defaults (Postgres credentials, a test captcha key, PayU sandbox credentials) — leave them as-is.
   - A few you invent yourself, no account needed: `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `JWT_SECRET`.
   - Two are optional and only unlock specific features once you set up a free third-party account — see "Optional: full functionality" below.

   Keep every variable name from `.env.example` present in `.env`, even if you leave its value blank for now — deleting a line entirely can stop the backend from starting.

4. Build and start everything:
   ```
   docker compose up --build
   ```

5. Once the backend logs settle and show `Started ApartmentbookingApplication`, open:
   - Frontend: http://localhost:4200
   - Backend API: http://localhost:8080
   - Swagger UI (API docs): http://localhost:8080/swagger-ui/index.html
   - PostgreSQL (if you want to connect a DB client): `localhost:5433`

   Log in as admin using the `ADMIN_EMAIL` / `ADMIN_PASSWORD` you set in `.env`.

## Optional: full functionality (email + payment confirmation)

Two features need real third-party credentials; without them the rest of the app runs normally.

**Sending emails** (booking confirmations, password reset) — needs a free account at [brevo.com](https://www.brevo.com). After signing up, go to Settings → SMTP & API → SMTP, and copy the SMTP login and generated key into `BREVO_SMTP_LOGIN` / `BREVO_SMTP_KEY`. Set `MAIL_FROM_ADDRESS` to a sender address you verified in Brevo.

Without this: **creating a reservation and requesting a password reset will both fail**, because each sends an email as part of the same request. Browsing apartments, registering, and logging in are unaffected.

**Confirming PayU payments automatically** — needs a free account at [ngrok.com](https://ngrok.com). Copy your authtoken from the ngrok dashboard into `NGROK_AUTHTOKEN`. On startup, the backend opens a public tunnel so PayU's sandbox can call back and confirm a payment.

Without this: you can still "pay" through the PayU sandbox UI, but the reservation will stay stuck on "awaiting payment", since PayU has no way to reach your local backend to confirm it succeeded.

## Admin account

On first startup, the backend automatically creates one admin account using the `ADMIN_EMAIL` and `ADMIN_PASSWORD` values from `.env`. The password is hashed with BCrypt before being stored — no plaintext credentials ever touch the database.

## Local backend development (IntelliJ)

Running the whole stack via Docker is the simplest option (see above). If you prefer running the backend directly in IntelliJ for hot-reload/debugging:

1. Start only the database: `docker compose up -d postgres`
2. In Run/Debug Configurations, either:
   - enable the **EnvFile** plugin and point it to the project's `.env`, or
   - manually set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` (and the rest of the variables from `.env`) as Environment Variables.

Note: `SPRING_DATASOURCE_URL` differs between the two setups — `jdbc:postgresql://localhost:5433/<POSTGRES_DB>` when running the backend locally, vs `jdbc:postgresql://postgres:5432/<POSTGRES_DB>` (the Docker service name) when running everything through Docker Compose.

## Running tests

- Backend: `cd backend && mvn test` (JUnit 5 + Mockito, no database required)
- Frontend: `cd frontend && npm test` (Jasmine/Karma)

## License

MIT
