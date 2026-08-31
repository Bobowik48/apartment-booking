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
import com.hubert.apartmentbooking.util.EmailTemplates;
import com.hubert.apartmentbooking.util.EmailTexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;
    private final AvailabilityService availabilityService;
    private final EmailService emailService;
    private final String frontendUrl;

    public ReservationService(ReservationRepository reservationRepository,
                              ApartmentRepository apartmentRepository,
                              UserRepository userRepository,
                              AvailabilityService availabilityService,
                              EmailService emailService,
                              @Value("${app.frontend.url}") String frontendUrl) {
        this.reservationRepository = reservationRepository;
        this.apartmentRepository = apartmentRepository;
        this.userRepository = userRepository;
        this.availabilityService = availabilityService;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request, Authentication authentication) {
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
        reservation.setStatus(ReservationStatus.PENDING_PAYMENT);
        reservation.setAccessToken(UUID.randomUUID().toString());
        reservation.setCreatedAt(LocalDateTime.now());

        Optional<User> loggedInUser = resolveAuthenticatedUser(authentication);
        if (loggedInUser.isPresent()) {
            User user = loggedInUser.get();
            reservation.setUser(user);
            reservation.setGuestName(user.getFullName());
            reservation.setGuestEmail(user.getEmail());
            reservation.setGuestPhone(user.getPhone());
        } else {
            reservation.setGuestName(request.guestName());
            reservation.setGuestEmail(request.guestEmail());
            reservation.setGuestPhone(request.guestPhone());
        }

        long nights = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        reservation.setTotalPrice(apartment.getPricePerNight().multiply(BigDecimal.valueOf(nights)));

        Reservation saved = reservationRepository.save(reservation);

        emailService.send(saved.getGuestEmail(), EmailTexts.RESERVATION_CONFIRMATION_SUBJECT,
                buildReservationConfirmationEmail(saved));

        return new ReservationResponse(saved.getId(), saved.getCheckInDate(), saved.getCheckOutDate(),
                saved.getGuestsCount(), saved.getTotalPrice(), saved.getStatus(), saved.getAccessToken());
    }

    private String buildReservationConfirmationEmail(Reservation reservation) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d.MM.yyyy");
        String greeting = EmailTexts.RESERVATION_CONFIRMATION_GREETING.formatted(reservation.getGuestName());
        String intro = EmailTexts.RESERVATION_CONFIRMATION_INTRO.formatted(
                reservation.getCheckInDate().format(dateFormatter),
                reservation.getCheckOutDate().format(dateFormatter),
                reservation.getGuestsCount(),
                reservation.getTotalPrice());
        String bodyHtml = "<p>%s</p><p>%s</p>".formatted(greeting, intro);
        String detailsUrl = "%s/reservation-details?token=%s".formatted(frontendUrl, reservation.getAccessToken());

        return EmailTemplates.button(
                EmailTexts.RESERVATION_CONFIRMATION_SUBJECT,
                bodyHtml,
                EmailTexts.RESERVATION_CONFIRMATION_BUTTON_TEXT,
                detailsUrl,
                EmailTexts.RESERVATION_CONFIRMATION_FOOTER);
    }

    private Optional<User> resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return userRepository.findByEmail(authentication.getName());
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