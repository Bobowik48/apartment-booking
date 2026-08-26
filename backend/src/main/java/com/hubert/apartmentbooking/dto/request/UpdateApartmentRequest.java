package com.hubert.apartmentbooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateApartmentRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String street,
        String apartmentNumber,
        @NotBlank String district,
        @NotBlank String city,
        @NotNull @Positive BigDecimal pricePerNight,
        @NotNull @Positive Integer maxGuests,
        @NotNull @Positive BigDecimal area,
        @NotNull @Positive Integer floor
) {
}