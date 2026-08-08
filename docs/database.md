# Database Schema

## Core entities (MVP)

### User
| Field | Type |
|---|---|
| id | Long |
| email | String |
| passwordHash | String |
| role | Role (enum: USER, ADMIN) |
| createdAt | LocalDateTime |

### Apartment
| Field | Type |
|---|---|
| id | Long |
| name | String |
| description | String |
| address | String |
| pricePerNight | BigDecimal |
| maxGuests | Integer |

### Reservation
| Field | Type |
|---|---|
| id | Long |
| user | User (ManyToOne) |
| apartment | Apartment (ManyToOne) |
| checkInDate | LocalDate |
| checkOutDate | LocalDate |
| status | ReservationStatus (enum: PENDING, CONFIRMED, CANCELLED, COMPLETED) |
| totalPrice | BigDecimal |
| createdAt | LocalDateTime |

### BlockedDate
| Field | Type |
|---|---|
| id | Long |
| apartment | Apartment (ManyToOne) |
| startDate | LocalDate |
| endDate | LocalDate |
| reason | String (nullable) |

## Later (not implemented yet)
- Payment — po integracji z PayU
- PasswordResetToken — po implementacji flow resetu hasła
- RefreshToken — po implementacji JWT refresh flow