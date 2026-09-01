package com.hubert.apartmentbooking.dto.response;

import com.hubert.apartmentbooking.model.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Integer guestsCount,
        BigDecimal totalPrice,
        ReservationStatus status,
        String accessToken,
        String guestName,
        String guestEmail,
        String guestPhone,
        String apartmentName,
        String apartmentStreet,
        String apartmentNumber,
        String apartmentCity
) {
}