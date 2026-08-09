package com.hubert.apartmentbooking.dto.request;

import java.time.LocalDate;

public record CreateReservationRequest(
        Long apartmentId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Integer guestsCount,
        String guestName,
        String guestEmail,
        String guestPhone,
        Long userId
) {
}