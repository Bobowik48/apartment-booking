# Architecture

## Overview

Apartment Booking is a three-tier application: an Angular single-page app talks to a Spring Boot REST API, which is the only thing that talks to PostgreSQL. All three run as separate Docker containers on one bridge network.

```
┌─────────────┐        HTTP (JSON, JWT)        ┌──────────────┐        JDBC        ┌────────────┐
│   Browser   │ ─────────────────────────────▶ │  Spring Boot  │ ─────────────────▶ │ PostgreSQL │
│  (Angular)  │ ◀───────────────────────────── │   REST API    │ ◀───────────────── │            │
└─────────────┘                                └──────┬───────┘                    └────────────┘
                                                        │
                                          ┌─────────────┼──────────────┐
                                          ▼             ▼              ▼
                                       Brevo         PayU          ngrok
                                     (SMTP email)  (payments)   (dev webhook tunnel)
```

One detail that trips people up: the browser talks **directly** to the backend's published port (`localhost:8080`), not through nginx. The frontend container's nginx only serves the compiled Angular bundle (`try_files ... /index.html`, a plain SPA fallback) — it does no API proxying. That's why the backend has an explicit CORS policy scoped to `http://localhost:4200` rather than same-origin.

## Frontend — Angular, standalone & signal-based

- **No NgModules.** Every component is `standalone`, and every route lazy-loads its component with `loadComponent` (`app.routes.ts`), so the initial bundle is just the home page — everything else is fetched on navigation.
- **Signals over manual subscriptions.** State that used to mean juggling `Observable`/`Subscription` pairs is plain `signal`/`computed` — e.g. on the booking page, total price and form validity are computed signals that recalculate themselves the moment the user changes dates or guest count, with no manual change-detection wiring.
- **Auth is two functions, not two classes.** `authGuard` / `adminGuard` are `CanActivateFn`s that gate routes, and `jwtInterceptor` is an `HttpInterceptorFn` that attaches the bearer token to outgoing requests — both wired in via `provideRouter`/`provideHttpClient` in `app.config.ts`, no legacy class-based DI boilerplate.

| Route | Guard | Component |
|---|---|---|
| `/` | — | Home (apartment showcase + gallery) |
| `/booking` | — | Booking (calendar, guest form, submit) |
| `/reservation-details` | — | Guest/owner view of one reservation, via `?token=` |
| `/login`, `/register` | — | Auth |
| `/forgot-password`, `/reset-password` | — | Password recovery |
| `/my-reservations` | `authGuard` | Logged-in user's reservation list |
| `/account`, `/change-password` | `authGuard` | Profile management |
| `/admin` | `adminGuard` | Apartment, photo & reservation management |

## Backend — Spring Boot, layered & stateless

- **Controller → Service → Repository, one direction only.** Controllers never touch repositories directly; all business rules live in services, which is what makes them independently unit-testable with Mockito and no Spring context (see `backend/src/test/.../service/`).
- **Stateless JWT auth.** `JwtAuthenticationFilter` (a `OncePerRequestFilter`) reads the `Authorization: Bearer` header on every request, validates the token, and populates Spring Security's context for that request only. There is no server-side session — nothing to make sticky if this ever sits behind a load balancer.
- **One consistent error contract.** Every domain exception (`ApartmentNotFoundException`, `DatesNotAvailableException`, `InvalidPayUSignatureException`, `ReservationAlreadyProcessedException`, …) is mapped by a global exception handler to one of a fixed set of string error codes (see `Constants`). The frontend never parses free-text error messages — it looks up the code and shows a localized message.

| Path | Access | Covers |
|---|---|---|
| `/api/auth/**` | public | register, login, forgot/reset password |
| `/api/apartments/**` | public read, `ADMIN` write | listing + admin edits |
| `/api/availability` | public | calendar of free/blocked dates |
| `/api/reservations/**` | public + `authenticated` for `/my` | create, guest lookup by token, own list |
| `/api/payments/**` | public (PayU redirects/webhooks land here) | init payment, PayU notification |
| `/api/users/me` | `authenticated` | profile, change password |
| `/api/admin/**` | `ADMIN` | apartment/photo/reservation administration |

Full request/response shapes are in Swagger UI (`/swagger-ui/index.html`) once the backend is running — see the main [README](../README.md).

## Notable flows

**Guest booking, no account required.** Creating a reservation always returns a unique, unguessable `accessToken` — that token, not a login, is how a guest gets back to their reservation (`/reservation-details?token=...`), sent to them by email. A logged-in user gets the same reservation, just linked to their account too.

**Payment confirmation (PayU).** `PaymentController` opens a PayU order and redirects the guest to PayU's hosted payment page. PayU calls back on a webhook with the outcome; before anything is trusted, the payload's signature is independently recomputed (MD5 of the raw body + a shared secret key) and compared — so a forged "payment succeeded" request can't confirm someone else's reservation. Locally, PayU's sandbox can't reach `localhost`, so on startup the backend optionally opens an `ngrok` tunnel and PayU is told to call that public URL instead (see the README for `NGROK_AUTHTOKEN` setup).

**Password reset.** A `PasswordResetToken` is emailed with a short validity window and a per-request cooldown, and is single-use.

**Idle reservations clean themselves up.** A scheduled job cancels any reservation still `PENDING_PAYMENT` past a configurable window (`RESERVATION_PENDING_PAYMENT_EXPIRATION_MINUTES`), so someone who starts checkout and abandons it doesn't permanently block those dates for everyone else.

## Infrastructure

Three Docker Compose services on one bridge network:

- **postgres** — data lives in a named volume (`postgres_data`), so it survives container rebuilds.
- **backend** — bind-mounts `./backend/uploads` from the host, so apartment photos uploaded through the admin panel survive a `docker compose up --build` instead of vanishing with the container's filesystem.
- **frontend** — a two-stage build: Node compiles the Angular app, then the static output is handed to a minimal `nginx:alpine` that just serves it.

Containers reach each other by service name inside that network (the backend talks to `postgres:5432`), but the browser sits outside it entirely and hits the host-published ports directly (`4200`, `8080`).

## Testing

**Backend:** JUnit 5 + Mockito. Every service's dependencies (repositories, `JwtService`, `PayUClient`, `EmailService`, …) are mocked, so the suite needs no database and no Spring context — fast and fully deterministic.

**Frontend:** Jasmine/Karma, using Angular's modern DI-based testing utilities (`provideHttpClient` + `provideHttpClientTesting`, `provideRouter`) rather than the older `HttpClientTestingModule`/`RouterTestingModule`.

See the main [README](../README.md) for how to run both.