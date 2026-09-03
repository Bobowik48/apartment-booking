package com.hubert.apartmentbooking.service;

import com.hubert.apartmentbooking.model.Apartment;
import com.hubert.apartmentbooking.model.BlockedDate;
import com.hubert.apartmentbooking.model.Reservation;
import com.hubert.apartmentbooking.model.enums.ReservationStatus;
import com.hubert.apartmentbooking.repository.BlockedDateRepository;
import com.hubert.apartmentbooking.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AvailabilityService#getUnavailableDates}, the function that powers the
 * booking calendar. It must expand both reservations and admin-blocked date ranges into
 * individual unavailable dates, clipped to the requested [from, to) window.
 */
@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private BlockedDateRepository blockedDateRepository;

    private AvailabilityService availabilityService;

    private static final Long APARTMENT_ID = 1L;

    @BeforeEach
    void setUp() {
        availabilityService = new AvailabilityService(reservationRepository, blockedDateRepository);
    }

    private Reservation reservationFor(LocalDate checkIn, LocalDate checkOut) {
        Apartment apartment = new Apartment();
        apartment.setId(APARTMENT_ID);
        Reservation reservation = new Reservation();
        reservation.setApartment(apartment);
        reservation.setCheckInDate(checkIn);
        reservation.setCheckOutDate(checkOut);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        return reservation;
    }

    private BlockedDate blockFor(LocalDate start, LocalDate end) {
        BlockedDate blocked = new BlockedDate();
        blocked.setStartDate(start);
        blocked.setEndDate(end);
        return blocked;
    }

    @Test
    void returnsEachNightOfAReservation_excludingTheCheckOutDate() {
        LocalDate from = LocalDate.of(2026, 9, 1);
        LocalDate to = LocalDate.of(2026, 9, 30);
        Reservation reservation = reservationFor(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 13));

        when(blockedDateRepository.findOverlapping(APARTMENT_ID, from, to)).thenReturn(List.of());
        when(reservationRepository.findOverlapping(eq(APARTMENT_ID), eq(from), eq(to), any(ReservationStatus.class)))
                .thenReturn(List.of(reservation));

        List<LocalDate> unavailable = availabilityService.getUnavailableDates(APARTMENT_ID, from, to);

        assertThat(unavailable).containsExactly(
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 11),
                LocalDate.of(2026, 9, 12));
        assertThat(unavailable).doesNotContain(LocalDate.of(2026, 9, 13));
    }

    @Test
    void excludesCancelledReservationsFromTheOverlapQuery() {
        LocalDate from = LocalDate.of(2026, 9, 1);
        LocalDate to = LocalDate.of(2026, 9, 30);

        when(blockedDateRepository.findOverlapping(APARTMENT_ID, from, to)).thenReturn(List.of());
        when(reservationRepository.findOverlapping(APARTMENT_ID, from, to, ReservationStatus.CANCELLED))
                .thenReturn(List.of());

        availabilityService.getUnavailableDates(APARTMENT_ID, from, to);

        // Verifies the repository is asked to exclude CANCELLED reservations specifically.
        org.mockito.Mockito.verify(reservationRepository)
                .findOverlapping(APARTMENT_ID, from, to, ReservationStatus.CANCELLED);
    }

    @Test
    void includesEveryDayOfABlockedRange_inclusiveOfBothEndpoints() {
        LocalDate from = LocalDate.of(2026, 9, 1);
        LocalDate to = LocalDate.of(2026, 9, 30);
        BlockedDate blocked = blockFor(LocalDate.of(2026, 9, 20), LocalDate.of(2026, 9, 22));

        when(blockedDateRepository.findOverlapping(APARTMENT_ID, from, to)).thenReturn(List.of(blocked));
        when(reservationRepository.findOverlapping(eq(APARTMENT_ID), eq(from), eq(to), any(ReservationStatus.class)))
                .thenReturn(List.of());

        List<LocalDate> unavailable = availabilityService.getUnavailableDates(APARTMENT_ID, from, to);

        assertThat(unavailable).containsExactly(
                LocalDate.of(2026, 9, 20),
                LocalDate.of(2026, 9, 21),
                LocalDate.of(2026, 9, 22));
    }

    @Test
    void clipsDatesOutsideTheRequestedWindow() {
        LocalDate from = LocalDate.of(2026, 9, 10);
        LocalDate to = LocalDate.of(2026, 9, 15);
        // Reservation spans well beyond the requested window on both sides.
        Reservation reservation = reservationFor(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        when(blockedDateRepository.findOverlapping(APARTMENT_ID, from, to)).thenReturn(List.of());
        when(reservationRepository.findOverlapping(eq(APARTMENT_ID), eq(from), eq(to), any(ReservationStatus.class)))
                .thenReturn(List.of(reservation));

        List<LocalDate> unavailable = availabilityService.getUnavailableDates(APARTMENT_ID, from, to);

        assertThat(unavailable).allMatch(date -> !date.isBefore(from) && date.isBefore(to));
        assertThat(unavailable).hasSize(5); // 10, 11, 12, 13, 14
    }

    @Test
    void returnsEmptyList_whenNothingOverlaps() {
        LocalDate from = LocalDate.of(2026, 9, 1);
        LocalDate to = LocalDate.of(2026, 9, 30);

        when(blockedDateRepository.findOverlapping(APARTMENT_ID, from, to)).thenReturn(List.of());
        when(reservationRepository.findOverlapping(eq(APARTMENT_ID), eq(from), eq(to), any(ReservationStatus.class)))
                .thenReturn(List.of());

        List<LocalDate> unavailable = availabilityService.getUnavailableDates(APARTMENT_ID, from, to);

        assertThat(unavailable).isEmpty();
    }
}
