package com.hubert.apartmentbooking.model;

import com.hubert.apartmentbooking.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "reservation_id", unique = true)
    private Reservation reservation;

    private String payuOrderId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private BigDecimal amount;

    private LocalDateTime createdAt;
}