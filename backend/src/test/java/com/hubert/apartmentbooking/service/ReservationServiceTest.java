package com.hubert.apartmentbooking.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReservationService}. All collaborators are mocked, so these tests run
 * without a database, HTTP server or mail server — they verify the business rules only:
 * date validation, availability checks, guest-vs-logged-in-user handling and pricing.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ApartmentRepository apartmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AvailabilityService availabilityService;
    @Mock
    private EmailService emailService;

    private ReservationService reservationService;

    private static final Long APARTMENT_ID = 1L;
    private static final String FRONTEND_URL = "http://localhost:4200";
    private static final int EXPIRATION_MINUTES = 15;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationRepository, apartmentRepository, userRepository,
                availabilityService, emailService, FRONTEND_URL, EXPIRATION_MINUTES);
    }

    private Apartment buildApartment() {
        Apartment apartment = new Apartment();
        apartment.setId(APARTMENT_ID);
        apartment.setName("Residenza Aurea");
        apartment.setStreet("Kwiatowa");
        apartment.setApartmentNumber("4");
        apartment.setCity("Warszawa");
        apartment.setPricePerNight(BigDecimal.valueOf(200));
        return apartment;
    }

    private CreateReservationRequest buildRequest(LocalDate checkIn, LocalDate checkOut) {
        return new CreateReservationRequest(APARTMENT_ID, checkIn, checkOut, 2,
                "Jan Kowalski", "jan@example.com", "+48600000000");
    }

    @Test
    void createReservation_savesReservationAndSendsEmail_forAnonymousGuest() {
        Apartment apartment = buildApartment();
        LocalDate checkIn = LocalDate.of(2026, 9, 10);
        LocalDate checkOut = LocalDate.of(2026, 9, 12);
        CreateReservationRequest request = buildRequest(checkIn, checkOut);

        when(apartmentRepository.findById(APARTMENT_ID)).thenReturn(Optional.of(apartment));
        when(availabilityService.getUnavailableDates(APARTMENT_ID, checkIn, checkOut)).thenReturn(List.of());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation r = invocation.getArgument(0);
            r.setId(42L);
            return r;
        });

        Authentication anonymous = new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

        ReservationResponse response = reservationService.createReservation(request, anonymous);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.status()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
        assertThat(response.totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(400)); // 2 nights * 200
        assertThat(response.guestEmail()).isEqualTo("jan@example.com");
        assertThat(response.accessToken()).isNotBlank();

        verify(userRepository, never()).findByEmail(any());
        verify(emailService, times(1)).send(eq("jan@example.com"), any(), any());
    }

    @Test
    void createReservation_usesLoggedInUserContactDetails_ignoringGuestFieldsFromRequest() {
        Apartment apartment = buildApartment();
        LocalDate checkIn = LocalDate.of(2026, 9, 10);
        LocalDate checkOut = LocalDate.of(2026, 9, 12);
        CreateReservationRequest request = buildRequest(checkIn, checkOut);

        User loggedInUser = new User();
        loggedInUser.setId(7L);
        loggedInUser.setFullName("Anna Nowak");
        loggedInUser.setEmail("anna@example.com");
        loggedInUser.setPhone("+48700000000");

        when(apartmentRepository.findById(APARTMENT_ID)).thenReturn(Optional.of(apartment));
        when(availabilityService.getUnavailableDates(APARTMENT_ID, checkIn, checkOut)).thenReturn(List.of());
        when(userRepository.findByEmail("anna@example.com")).thenReturn(Optional.of(loggedInUser));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Authentication authenticated = new UsernamePasswordAuthenticationToken(
                "anna@example.com", null, AuthorityUtils.createAuthorityList("ROLE_USER"));

        ReservationResponse response = reservationService.createReservation(request, authenticated);

        assertThat(response.guestName()).isEqualTo("Anna Nowak");
        assertThat(response.guestEmail()).isEqualTo("anna@example.com");
        assertThat(response.guestPhone()).isEqualTo("+48700000000");
    }

    @Test
    void createReservation_throws_whenCheckOutIsNotAfterCheckIn() {
        Apartment apartment = buildApartment();
        LocalDate sameDay = LocalDate.of(2026, 9, 10);
        CreateReservationRequest request = buildRequest(sameDay, sameDay);

        when(apartmentRepository.findById(APARTMENT_ID)).thenReturn(Optional.of(apartment));

        assertThatThrownBy(() -> reservationService.createReservation(request, null))
                .isInstanceOf(DatesNotAvailableException.class);

        verify(reservationRepository, never()).save(any());
        verify(emailService, never()).send(any(), any(), any());
    }

    @Test
    void createReservation_throws_whenDatesAreNotAvailable() {
        Apartment apartment = buildApartment();
        LocalDate checkIn = LocalDate.of(2026, 9, 10);
        LocalDate checkOut = LocalDate.of(2026, 9, 12);
        CreateReservationRequest request = buildRequest(checkIn, checkOut);

        when(apartmentRepository.findById(APARTMENT_ID)).thenReturn(Optional.of(apartment));
        when(availabilityService.getUnavailableDates(APARTMENT_ID, checkIn, checkOut))
                .thenReturn(List.of(LocalDate.of(2026, 9, 11)));

        assertThatThrownBy(() -> reservationService.createReservation(request, null))
                .isInstanceOf(DatesNotAvailableException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_throws_whenApartmentDoesNotExist() {
        CreateReservationRequest request = buildRequest(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 12));

        when(apartmentRepository.findById(APARTMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(request, null))
                .isInstanceOf(ApartmentNotFoundException.class);

        verify(availabilityService, never()).getUnavailableDates(anyLong(), any(), any());
    }

    @Test
    void getByAccessToken_returnsReservation_whenFound() {
        Apartment apartment = buildApartment();
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setApartment(apartment);
        reservation.setCheckInDate(LocalDate.of(2026, 9, 10));
        reservation.setCheckOutDate(LocalDate.of(2026, 9, 12));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setAccessToken("token-123");
        reservation.setTotalPrice(BigDecimal.valueOf(400));

        when(reservationRepository.findByAccessToken("token-123")).thenReturn(Optional.of(reservation));

        ReservationResponse response = reservationService.getByAccessToken("token-123");

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void getByAccessToken_throws_whenTokenUnknown() {
        when(reservationRepository.findByAccessToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getByAccessToken("missing"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void getMyReservations_mapsAllReservationsForTheUser() {
        Apartment apartment = buildApartment();
        Reservation r1 = new Reservation();
        r1.setId(1L);
        r1.setApartment(apartment);
        r1.setCheckInDate(LocalDate.of(2026, 9, 10));
        r1.setCheckOutDate(LocalDate.of(2026, 9, 12));
        r1.setStatus(ReservationStatus.CONFIRMED);
        r1.setTotalPrice(BigDecimal.valueOf(400));

        when(reservationRepository.findByUser_EmailOrderByIdDesc("jan@example.com")).thenReturn(List.of(r1));

        List<ReservationResponse> result = reservationService.getMyReservations("jan@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
    }
}
