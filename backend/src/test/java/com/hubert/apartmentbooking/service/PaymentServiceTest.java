package com.hubert.apartmentbooking.service;

import com.hubert.apartmentbooking.config.BackendUrlHolder;
import com.hubert.apartmentbooking.dto.response.PaymentInitResponse;
import com.hubert.apartmentbooking.exception.InvalidPayUSignatureException;
import com.hubert.apartmentbooking.exception.ReservationAlreadyProcessedException;
import com.hubert.apartmentbooking.model.Apartment;
import com.hubert.apartmentbooking.model.Payment;
import com.hubert.apartmentbooking.model.Reservation;
import com.hubert.apartmentbooking.model.enums.PaymentStatus;
import com.hubert.apartmentbooking.model.enums.ReservationStatus;
import com.hubert.apartmentbooking.repository.PaymentRepository;
import com.hubert.apartmentbooking.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PaymentService}. {@link PayUClient} (the actual HTTP call to PayU) and
 * {@link EmailService} are mocked out entirely. A real Jackson {@code ObjectMapper} (via
 * {@code JsonMapper.builder().build()}) is used for the notification-parsing tests so we don't
 * need to reach into PaymentService's private nested notification record type.
 *
 * <p>Note: if this project's Jackson version exposes {@code new ObjectMapper()} directly instead
 * of requiring {@code JsonMapper.builder().build()}, feel free to simplify the constructor call
 * in {@link #setUp()} accordingly — the rest of the test is unaffected either way.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PayUClient payUClient;
    @Mock
    private EmailService emailService;
    @Mock
    private BackendUrlHolder backendUrlHolder;

    private PaymentService paymentService;

    private static final String FRONTEND_URL = "http://localhost:4200";
    private static final String SECOND_KEY = "test-second-key";
    private static final int EXPIRATION_MINUTES = 15;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(reservationRepository, paymentRepository, payUClient,
                emailService, JsonMapper.builder().build(), backendUrlHolder,
                FRONTEND_URL, SECOND_KEY, EXPIRATION_MINUTES);
    }

    private Reservation buildReservation(ReservationStatus status) {
        Apartment apartment = new Apartment();
        apartment.setName("Residenza Aurea");

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setApartment(apartment);
        reservation.setStatus(status);
        reservation.setAccessToken("token-123");
        reservation.setCheckInDate(java.time.LocalDate.of(2026, 9, 10));
        reservation.setCheckOutDate(java.time.LocalDate.of(2026, 9, 12));
        reservation.setGuestsCount(2);
        reservation.setTotalPrice(BigDecimal.valueOf(400));
        reservation.setGuestEmail("jan@example.com");
        reservation.setGuestName("Jan Kowalski");
        return reservation;
    }

    // ### initPayment ###

    @Test
    void initPayment_createsPendingPaymentAndReturnsRedirectUrl() {
        Reservation reservation = buildReservation(ReservationStatus.PENDING_PAYMENT);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(backendUrlHolder.get()).thenReturn("http://localhost:8080");
        when(paymentRepository.findByReservation_Id(1L)).thenReturn(Optional.empty());
        when(payUClient.createOrder(any(), any(), any(), any(), anyInt()))
                .thenReturn(new PayUClient.OrderResult("payu-order-1", "https://pay.example.com/redirect"));

        PaymentInitResponse response = paymentService.initPayment(1L, "127.0.0.1");

        assertThat(response.redirectUrl()).isEqualTo("https://pay.example.com/redirect");
        verify(paymentRepository).save(argThat(p -> p != null && p.getStatus() == PaymentStatus.PENDING));
    }

    @Test
    void initPayment_throws_whenReservationAlreadyProcessed() {
        Reservation reservation = buildReservation(ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> paymentService.initPayment(1L, "127.0.0.1"))
                .isInstanceOf(ReservationAlreadyProcessedException.class);

        verify(payUClient, never()).createOrder(any(), any(), any(), any(), anyInt());
    }

    @Test
    void initPayment_throws_whenReservationNotFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.initPayment(99L, "127.0.0.1"))
                .isInstanceOf(NoSuchElementException.class);
    }

    // ### handleNotification ###

    @Test
    void handleNotification_throws_whenSignatureIsInvalid() {
        String body = "{\"order\":{\"orderId\":\"payu-order-1\",\"status\":\"COMPLETED\"}}";

        assertThatThrownBy(() -> paymentService.handleNotification(body, "signature=deadbeef;algorithm=MD5"))
                .isInstanceOf(InvalidPayUSignatureException.class);

        verify(paymentRepository, never()).findByPayuOrderId(any());
    }

    @Test
    void handleNotification_confirmsReservationAndSendsEmail_onCompletedStatus() throws Exception {
        Reservation reservation = buildReservation(ReservationStatus.PENDING_PAYMENT);
        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findByPayuOrderId("payu-order-1")).thenReturn(Optional.of(payment));

        String body = "{\"order\":{\"orderId\":\"payu-order-1\",\"status\":\"COMPLETED\"}}";
        String header = validSignatureHeader(body);

        paymentService.handleNotification(body, header);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(emailService).send(eq("jan@example.com"), any(), any());
    }

    @Test
    void handleNotification_cancelsReservation_onCanceledStatus() throws Exception {
        Reservation reservation = buildReservation(ReservationStatus.PENDING_PAYMENT);
        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findByPayuOrderId("payu-order-1")).thenReturn(Optional.of(payment));

        String body = "{\"order\":{\"orderId\":\"payu-order-1\",\"status\":\"CANCELED\"}}";
        String header = validSignatureHeader(body);

        paymentService.handleNotification(body, header);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        verify(emailService, never()).send(any(), any(), any());
    }

    @Test
    void handleNotification_isIdempotent_whenPaymentIsNoLongerPending() throws Exception {
        Reservation reservation = buildReservation(ReservationStatus.CONFIRMED);
        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setStatus(PaymentStatus.COMPLETED); // already processed by an earlier notification

        when(paymentRepository.findByPayuOrderId("payu-order-1")).thenReturn(Optional.of(payment));

        String body = "{\"order\":{\"orderId\":\"payu-order-1\",\"status\":\"COMPLETED\"}}";
        String header = validSignatureHeader(body);

        paymentService.handleNotification(body, header);

        // Status must not be touched again and no duplicate confirmation email should be sent.
        verify(emailService, never()).send(any(), any(), any());
    }

    @Test
    void handleNotification_throws_whenPaymentUnknown() throws Exception {
        when(paymentRepository.findByPayuOrderId("unknown-order")).thenReturn(Optional.empty());

        String body = "{\"order\":{\"orderId\":\"unknown-order\",\"status\":\"COMPLETED\"}}";
        String header = validSignatureHeader(body);

        assertThatThrownBy(() -> paymentService.handleNotification(body, header))
                .isInstanceOf(NoSuchElementException.class);
    }

    private static String validSignatureHeader(String body) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] hash = digest.digest((body + SECOND_KEY).getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return "signature=" + sb + ";algorithm=MD5";
    }
}