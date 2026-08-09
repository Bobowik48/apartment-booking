package com.hubert.apartmentbooking.dto.request;

public record LoginRequest(
        String email,
        String password
) {
}