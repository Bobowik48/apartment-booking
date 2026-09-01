package com.hubert.apartmentbooking.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hubert.apartmentbooking.exception.PayUOrderCreationException;
import com.hubert.apartmentbooking.model.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.http.HttpClient;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.hubert.apartmentbooking.constants.Constants.PAYU_ORDER_CREATION_FAILED;

@Service
public class PayUClient {

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String posId;
    private static final Logger log = LoggerFactory.getLogger(PayUClient.class);

    private String cachedAccessToken;
    private Instant tokenExpiresAt = Instant.EPOCH;

    public PayUClient(@Value("${app.payu.base-url}") String baseUrl,
                      @Value("${app.payu.client-id}") String clientId,
                      @Value("${app.payu.client-secret}") String clientSecret,
                      @Value("${app.payu.pos-id}") String posId) {
        HttpClient jdkHttpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new JdkClientHttpRequestFactory(jdkHttpClient))
                .build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.posId = posId;
    }

    public record OrderResult(String payuOrderId, String redirectUrl) {
    }

    public OrderResult createOrder(Reservation reservation, String notifyUrl, String continueUrl,
                                   String customerIp, int validitySeconds) {
        String accessToken = getAccessToken();
        String amountInGrosze = toGrosze(reservation.getTotalPrice());

        Map<String, Object> body = Map.ofEntries(
                Map.entry("notifyUrl", notifyUrl),
                Map.entry("continueUrl", continueUrl),
                Map.entry("customerIp", customerIp),
                Map.entry("merchantPosId", posId),
                Map.entry("description", "Rezerwacja #" + reservation.getId() + " - Residenza Aurea"),
                Map.entry("currencyCode", "PLN"),
                Map.entry("totalAmount", amountInGrosze),
                Map.entry("extOrderId", "reservation-" + reservation.getId() + "-" + System.currentTimeMillis()),
                Map.entry("validityTime", validitySeconds),
                Map.entry("buyer", Map.of(
                        "email", reservation.getGuestEmail(),
                        "phone", reservation.getGuestPhone(),
                        "firstName", firstNameOf(reservation.getGuestName()),
                        "lastName", lastNameOf(reservation.getGuestName()),
                        "language", "pl"
                )),
                Map.entry("products", List.of(Map.of(
                        "name", "Rezerwacja #" + reservation.getId(),
                        "unitPrice", amountInGrosze,
                        "quantity", "1"
                )))
        );
        try {
            ResponseEntity<PayUOrderResponse> response = restClient.post()
                    .uri("/api/v2_1/orders")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(PayUOrderResponse.class);

            PayUOrderResponse orderResponse = response.getBody();
            if (orderResponse == null || orderResponse.redirectUri() == null) {
                throw new PayUOrderCreationException(PAYU_ORDER_CREATION_FAILED);
            }

            return new OrderResult(orderResponse.orderId(), orderResponse.redirectUri());
        } catch (Exception e) {
            log.error("PayU order creation failed for reservation {}", reservation.getId(), e);
            throw new PayUOrderCreationException(PAYU_ORDER_CREATION_FAILED);
        }
    }

    private synchronized String getAccessToken() {
        if (cachedAccessToken != null && Instant.now().isBefore(tokenExpiresAt)) {
            return cachedAccessToken;
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        PayUTokenResponse response = restClient.post()
                .uri("/pl/standard/user/oauth/authorize")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(PayUTokenResponse.class);

        if (response == null) {
            throw new PayUOrderCreationException(PAYU_ORDER_CREATION_FAILED);
        }

        cachedAccessToken = response.accessToken();
        tokenExpiresAt = Instant.now().plusSeconds(Math.max(response.expiresIn() - 60, 0));
        return cachedAccessToken;
    }

    private static String toGrosze(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    private static String firstNameOf(String fullName) {
        int idx = fullName.indexOf(' ');
        return idx > 0 ? fullName.substring(0, idx) : fullName;
    }

    private static String lastNameOf(String fullName) {
        int idx = fullName.indexOf(' ');
        return idx > 0 ? fullName.substring(idx + 1) : "";
    }

    private record PayUTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") int expiresIn
    ) {
    }

    private record PayUOrderResponse(String orderId, String redirectUri) {
    }
}