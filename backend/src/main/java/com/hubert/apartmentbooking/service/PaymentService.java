package com.hubert.apartmentbooking.service;

import com.hubert.apartmentbooking.config.BackendUrlHolder;
import com.hubert.apartmentbooking.constants.Constants;
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
import com.hubert.apartmentbooking.util.EmailTemplates;
import com.hubert.apartmentbooking.util.EmailTexts;
import com.hubert.apartmentbooking.util.PayUSignatureVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;

@Service
public class PaymentService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final PayUClient payUClient;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final BackendUrlHolder backendUrlHolder;
    private final String frontendUrl;
    private final String secondKey;
    private final int expirationMinutes;

    public PaymentService(ReservationRepository reservationRepository,
                          PaymentRepository paymentRepository,
                          PayUClient payUClient,
                          EmailService emailService,
                          ObjectMapper objectMapper,
                          BackendUrlHolder backendUrlHolder,
                          @Value("${app.frontend.url}") String frontendUrl,
                          @Value("${app.payu.second-key}") String secondKey,
                          @Value("${app.reservation.pending-payment-expiration-minutes}") int expirationMinutes) {
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.payUClient = payUClient;
        this.emailService = emailService;
        this.objectMapper = objectMapper;
        this.backendUrlHolder = backendUrlHolder;
        this.frontendUrl = frontendUrl;
        this.secondKey = secondKey;
        this.expirationMinutes = expirationMinutes;
    }

    @Transactional
    public PaymentInitResponse initPayment(Long reservationId, String customerIp) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException(Constants.RESERVATION_NOT_FOUND));

        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new ReservationAlreadyProcessedException(Constants.RESERVATION_ALREADY_PROCESSED);
        }

        String notifyUrl = backendUrlHolder.get() + Constants.PAYMENTS_PATH + Constants.NOTIFY_ENDPOINT;
        String continueUrl = frontendUrl + "/reservation-details?token=" + reservation.getAccessToken();
        int validitySeconds = expirationMinutes * 60;

        PayUClient.OrderResult orderResult = payUClient.createOrder(
                reservation, notifyUrl, continueUrl, customerIp, validitySeconds);

        Payment payment = paymentRepository.findByReservation_Id(reservation.getId())
                .orElseGet(Payment::new);
        payment.setReservation(reservation);
        payment.setPayuOrderId(orderResult.payuOrderId());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(reservation.getTotalPrice());
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return new PaymentInitResponse(orderResult.redirectUrl());
    }

    @Transactional
    public void handleNotification(String rawBody, String signatureHeader) {
        if (!PayUSignatureVerifier.isValid(rawBody, signatureHeader, secondKey)) {
            throw new InvalidPayUSignatureException(Constants.INVALID_PAYU_SIGNATURE);
        }

        PayUNotification notification;
        try {
            notification = objectMapper.readValue(rawBody, PayUNotification.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed PayU notification body");
        }

        Payment payment = paymentRepository.findByPayuOrderId(notification.order().orderId())
                .orElseThrow(() -> new NoSuchElementException(Constants.PAYMENT_NOT_FOUND));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        Reservation reservation = payment.getReservation();

        switch (notification.order().status()) {
            case "COMPLETED" -> {
                payment.setStatus(PaymentStatus.COMPLETED);
                reservation.setStatus(ReservationStatus.CONFIRMED);
                emailService.send(reservation.getGuestEmail(), EmailTexts.RESERVATION_CONFIRMATION_SUBJECT,
                        buildReservationConfirmationEmail(reservation));
            }
            case "CANCELED" -> {
                payment.setStatus(PaymentStatus.CANCELED);
                reservation.setStatus(ReservationStatus.CANCELLED);
            }
            default -> {
                // NEW / PENDING / WAITING_FOR_CONFIRMATION — jeszcze nic nie robimy
            }
        }
    }

    private String buildReservationConfirmationEmail(Reservation reservation) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d.MM.yyyy");
        Apartment apartment = reservation.getApartment();

        String greeting = EmailTexts.RESERVATION_CONFIRMATION_GREETING.formatted(reservation.getGuestName());
        String intro = EmailTexts.RESERVATION_CONFIRMATION_INTRO.formatted(
                reservation.getCheckInDate().format(dateFormatter),
                reservation.getCheckOutDate().format(dateFormatter),
                reservation.getGuestsCount(),
                reservation.getTotalPrice());

        StringBuilder bodyHtml = new StringBuilder();
        bodyHtml.append("<p>").append(greeting).append("</p>");
        bodyHtml.append("<p>").append(intro).append("</p>");

        if (hasAccessCodes(apartment)) {
            String accessDetails = EmailTexts.RESERVATION_CONFIRMATION_ACCESS_DETAILS.formatted(
                    apartment.getBuildingEntranceCode(),
                    apartment.getKeyBoxCode(),
                    apartment.getApartmentNumber());
            bodyHtml.append("<p>").append(EmailTexts.RESERVATION_CONFIRMATION_ACCESS_INTRO).append("</p>");
            bodyHtml.append("<p>").append(accessDetails).append("</p>");
        }

        String detailsUrl = "%s/reservation-details?token=%s".formatted(frontendUrl, reservation.getAccessToken());

        return EmailTemplates.button(
                EmailTexts.RESERVATION_CONFIRMATION_SUBJECT,
                bodyHtml.toString(),
                EmailTexts.RESERVATION_CONFIRMATION_BUTTON_TEXT,
                detailsUrl,
                EmailTexts.RESERVATION_CONFIRMATION_FOOTER);
    }

    private boolean hasAccessCodes(Apartment apartment) {
        return apartment.getBuildingEntranceCode() != null && !apartment.getBuildingEntranceCode().isBlank()
                && apartment.getKeyBoxCode() != null && !apartment.getKeyBoxCode().isBlank();
    }

    private record PayUNotification(Order order) {
        private record Order(String orderId, String status) {
        }
    }
}