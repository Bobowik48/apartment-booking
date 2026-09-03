# Database Schema

Schema is managed by Hibernate (`spring.jpa.hibernate.ddl-auto=update`) — tables are created/updated automatically from these entities on backend startup, no manual migration step needed.

## Entities

### User (`users`)
| Field | Type | Notes |
|---|---|---|
| id | Long | PK, auto-increment |
| fullName | String | |
| email | String | used as login |
| phone | String | |
| passwordHash | String | BCrypt hash — plaintext passwords are never stored |
| role | Role (enum: `USER`, `ADMIN`) | |
| createdAt | LocalDateTime | |

### Apartment (`apartments`)
| Field | Type |
|---|---|
| id | Long |
| name | String |
| description | String |
| street | String |
| apartmentNumber | String |
| district | String |
| city | String |
| pricePerNight | BigDecimal |
| maxGuests | Integer |
| area | BigDecimal |
| floor | Integer |
| buildingEntranceCode | String |
| keyBoxCode | String |

### ApartmentPhoto (`apartment_photos`)
| Field | Type | Notes |
|---|---|---|
| id | Long | |
| apartment | Apartment (ManyToOne) | |
| fileName | String | filename on disk under `app.upload.dir` (`./uploads` by default), served at `/uploads/apartments/{apartmentId}/{fileName}` |
| displayOrder | Integer | controls gallery ordering |
| altText | String | |

### Reservation (`reservations`)
| Field | Type | Notes |
|---|---|---|
| id | Long | |
| user | User (ManyToOne, nullable) | null for guest bookings (no account) |
| apartment | Apartment (ManyToOne) | |
| guestName | String | |
| guestEmail | String | |
| guestPhone | String | |
| checkInDate | LocalDate | |
| checkOutDate | LocalDate | |
| guestsCount | Integer | |
| totalPrice | BigDecimal | |
| status | ReservationStatus (enum: `PENDING_PAYMENT`, `CONFIRMED`, `CANCELLED`, `COMPLETED`) | |
| accessToken | String (unique) | lets a guest without an account view/pay for their reservation via a private link (`/reservation-details?token=...`) |
| createdAt | LocalDateTime | |

Unpaid reservations (`PENDING_PAYMENT`) past a configurable expiration window (`RESERVATION_PENDING_PAYMENT_EXPIRATION_MINUTES`, default 15 min) are automatically cancelled by a scheduled job (`ReservationExpirationJob`).

### BlockedDate (`blocked_dates`)
| Field | Type | Notes |
|---|---|---|
| id | Long | |
| apartment | Apartment (ManyToOne) | |
| startDate | LocalDate | |
| endDate | LocalDate | |
| reason | String (nullable) | manual admin block, unrelated to a reservation |

### Payment (`payments`)
| Field | Type | Notes |
|---|---|---|
| id | Long | |
| reservation | Reservation (OneToOne, unique) | |
| payuOrderId | String | PayU's order id, used to match incoming webhook notifications |
| status | PaymentStatus (enum: `PENDING`, `COMPLETED`, `CANCELED`) | |
| amount | BigDecimal | |
| createdAt | LocalDateTime | |

### PasswordResetToken (`password_reset_tokens`)
| Field | Type | Notes |
|---|---|---|
| id | Long | |
| token | String (unique) | sent to the user by email, single use |
| user | User (ManyToOne) | |
| createdAt | LocalDateTime | |
| expiresAt | LocalDateTime | validity window set by `RESET_TOKEN_VALIDITY_HOURS` (default 1h) |

## Relationships
- `Apartment` 1—N `ApartmentPhoto`, `BlockedDate`, `Reservation`
- `Reservation` 1—1 `Payment` (at most one payment attempt record per reservation)
- `User` 1—N `Reservation` (logged-in bookings only), 1—N `PasswordResetToken`

## Not implemented
- **Refresh tokens** — authentication is a single JWT (`JWT_EXPIRATION_MS`, default 24h) with no refresh flow; once it expires the user simply logs in again.