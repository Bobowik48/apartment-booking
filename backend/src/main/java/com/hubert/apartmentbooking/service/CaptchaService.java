package com.hubert.apartmentbooking.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class CaptchaService {

    private static final String VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    @Value("${app.captcha.secret}")
    private String secret;

    private final RestClient restClient = RestClient.create();

    public boolean verify(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        TurnstileResponse response = restClient.post()
                .uri(VERIFY_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("secret=" + secret + "&response=" + token)
                .retrieve()
                .body(TurnstileResponse.class);

        return response != null && response.success();
    }

    private record TurnstileResponse(
            boolean success,
            @JsonProperty("error-codes") List<String> errorCodes
    ) {
    }
}