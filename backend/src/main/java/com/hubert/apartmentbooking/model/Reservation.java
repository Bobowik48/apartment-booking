package com.hubert.apartmentbooking.model;

import com.hubert.apartmentbooking.model.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    private String guestName;

    private String guestEmail;

    private String guestPhone;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private Integer guestsCount;

    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column(unique = true)
    private String accessToken;

    private LocalDateTime createdAt;
}