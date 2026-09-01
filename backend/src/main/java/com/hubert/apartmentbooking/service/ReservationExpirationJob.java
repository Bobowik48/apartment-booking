package com.hubert.apartmentbooking.service;

import com.hubert.apartmentbooking.model.Reservation;
import com.hubert.apartmentbooking.model.enums.ReservationStatus;
import com.hubert.apartmentbooking.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReservationExpirationJob {

    private final ReservationRepository reservationRepository;
    private final int expirationMinutes;

    public ReservationExpirationJob(ReservationRepository reservationRepository,
                                    @Value("${app.reservation.pending-payment-expiration-minutes}") int expirationMinutes) {
        this.reservationRepository = reservationRepository;
        this.expirationMinutes = expirationMinutes;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expireUnpaidReservations() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(expirationMinutes);
        List<Reservation> expired = reservationRepository.findByStatusAndCreatedAtBefore(
                ReservationStatus.PENDING_PAYMENT, cutoff);

        for (Reservation reservation : expired) {
            reservation.setStatus(ReservationStatus.CANCELLED);
        }
        reservationRepository.saveAll(expired);
    }
}