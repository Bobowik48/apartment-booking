package com.hubert.apartmentbooking.service;

import com.hubert.apartmentbooking.constants.Constants;
import com.hubert.apartmentbooking.dto.request.CreateReservationRequest;
import com.hubert.apartmentbooking.dto.response.ReservationResponse;
import com.hubert.apartmentbooking.exception.ApartmentNotFoundException;
import com.hubert.apartmentbooking.exception.DatesNotAvailableException;
import com.hubert.apartmentbooking.model.Apartment;
import com.hubert.apartmentbooking.model.Reservation;
import com.hubert.apartmentbooking.model.User;
import com.hubert.apartmentbooking.model.enums.ReservationStatus;
import com.hubert.apartmentbooking.repository.ApartmentRepository;
import com.hubert.apartmentbooking.repository.ReservationRepository;
import com.hubert.apartmentbooking.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;
    private final AvailabilityService availabilityService;

    public ReservationService(ReservationRepository reservationRepository,
                              ApartmentRepository apartmentRepository,
                              UserRepository userRepository,
                              AvailabilityService availabilityService) {
        this.reservationRepository = reservationRepository;
        this.apartmentRepository = apartmentRepository;
        this.userRepository = userRepository;
        this.availabilityService = availabilityService;
    }

    public ReservationResponse createReservation(CreateReservationRequest request) {
        Apartment apartment = apartmentRepository.findById(request.apartmentId())
                .orElseThrow(() -> new ApartmentNotFoundException(
                        String.format(Constants.APARTMENT_NOT_FOUND, request.apartmentId())));

        if (!request.checkOutDate().isAfter(request.checkInDate())) {
            throw new DatesNotAvailableException(Constants.CHECK_OUT_BEFORE_CHECK_IN);
        }

        List<LocalDate> unavailableDates = availabilityService.getUnavailableDates(
                request.apartmentId(), request.checkInDate(), request.checkOutDate());

        if (!unavailableDates.isEmpty()) {
            throw new DatesNotAvailableException(Constants.DATES_NOT_AVAILABLE);
        }

        Reservation reservation = new Reservation();
        reservation.setApartment(apartment);
        reservation.setCheckInDate(request.checkInDate());
        reservation.setCheckOutDate(request.checkOutDate());
        reservation.setGuestsCount(request.guestsCount());
        reservation.setGuestName(request.guestName());
        reservation.setGuestEmail(request.guestEmail());
        reservation.setGuestPhone(request.guestPhone());
        reservation.setStatus(ReservationStatus.PENDING_PAYMENT);
        reservation.setAccessToken(UUID.randomUUID().toString());
        reservation.setCreatedAt(LocalDateTime.now());

        long nights = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        reservation.setTotalPrice(apartment.getPricePerNight().multiply(BigDecimal.valueOf(nights)));

        if (request.userId() != null) {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new NoSuchElementException(Constants.USER_NOT_FOUND));
            reservation.setUser(user);
        }

        Reservation saved = reservationRepository.save(reservation);

        return new ReservationResponse(saved.getId(), saved.getCheckInDate(), saved.getCheckOutDate(),
                saved.getGuestsCount(), saved.getTotalPrice(), saved.getStatus(), saved.getAccessToken());
    }

    public ReservationResponse getByAccessToken(String accessToken) {
        Reservation reservation = reservationRepository.findByAccessToken(accessToken)
                .orElseThrow(() -> new NoSuchElementException(Constants.RESERVATION_NOT_FOUND));

        return new ReservationResponse(reservation.getId(), reservation.getCheckInDate(), reservation.getCheckOutDate(),
                reservation.getGuestsCount(), reservation.getTotalPrice(), reservation.getStatus(), reservation.getAccessToken());
    }

    public List<ReservationResponse> getMyReservations(String email) {
        return reservationRepository.findByUser_EmailOrderByCheckInDateDesc(email).stream()
                .map(r -> new ReservationResponse(r.getId(), r.getCheckInDate(), r.getCheckOutDate(),
                        r.getGuestsCount(), r.getTotalPrice(), r.getStatus(), r.getAccessToken()))
                .toList();
    }
}