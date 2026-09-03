package com.hubert.apartmentbooking.service;

import com.hubert.apartmentbooking.model.Reservation;
import com.hubert.apartmentbooking.model.enums.ReservationStatus;
import com.hubert.apartmentbooking.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReservationExpirationJob}, the scheduled task that cancels reservations
 * left in PENDING_PAYMENT for too long (customer abandoned checkout without paying).
 */
@ExtendWith(MockitoExtension.class)
class ReservationExpirationJobTest {

    @Mock
    private ReservationRepository reservationRepository;

    private ReservationExpirationJob job;

    private static final int EXPIRATION_MINUTES = 15;

    @BeforeEach
    void setUp() {
        job = new ReservationExpirationJob(reservationRepository, EXPIRATION_MINUTES);
    }

    @Test
    void marksAllOverdueReservationsAsCancelled() {
        Reservation stale1 = new Reservation();
        stale1.setId(1L);
        stale1.setStatus(ReservationStatus.PENDING_PAYMENT);

        Reservation stale2 = new Reservation();
        stale2.setId(2L);
        stale2.setStatus(ReservationStatus.PENDING_PAYMENT);

        when(reservationRepository.findByStatusAndCreatedAtBefore(eq(ReservationStatus.PENDING_PAYMENT), any(LocalDateTime.class)))
                .thenReturn(List.of(stale1, stale2));

        job.expireUnpaidReservations();

        assertThat(stale1.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(stale2.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        verify(reservationRepository).saveAll(List.of(stale1, stale2));
    }

    @Test
    void usesACutoffOfExpirationMinutesInThePast() {
        when(reservationRepository.findByStatusAndCreatedAtBefore(eq(ReservationStatus.PENDING_PAYMENT), any(LocalDateTime.class)))
                .thenReturn(List.of());

        LocalDateTime before = LocalDateTime.now().minusMinutes(EXPIRATION_MINUTES);
        job.expireUnpaidReservations();
        LocalDateTime after = LocalDateTime.now().minusMinutes(EXPIRATION_MINUTES);

        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(reservationRepository).findByStatusAndCreatedAtBefore(eq(ReservationStatus.PENDING_PAYMENT), cutoffCaptor.capture());

        LocalDateTime cutoff = cutoffCaptor.getValue();
        assertThat(cutoff).isBetween(before.minusSeconds(2), after.plusSeconds(2));
    }

    @Test
    void doesNothingHarmful_whenThereAreNoExpiredReservations() {
        when(reservationRepository.findByStatusAndCreatedAtBefore(eq(ReservationStatus.PENDING_PAYMENT), any(LocalDateTime.class)))
                .thenReturn(List.of());

        job.expireUnpaidReservations();

        verify(reservationRepository).saveAll(List.of());
    }
}
