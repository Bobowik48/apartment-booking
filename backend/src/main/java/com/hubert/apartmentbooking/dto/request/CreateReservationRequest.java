package com.hubert.apartmentbooking.dto.request;

import com.hubert.apartmentbooking.constants.Constants;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CreateReservationRequest(
        @NotNull(message = Constants.REQUIRED_FIELD_MISSING)
        Long apartmentId,

        @NotNull(message = Constants.REQUIRED_FIELD_MISSING)
        @FutureOrPresent(message = Constants.CHECK_OUT_BEFORE_CHECK_IN)
        LocalDate checkInDate,

        @NotNull(message = Constants.REQUIRED_FIELD_MISSING)
        @Future(message = Constants.CHECK_OUT_BEFORE_CHECK_IN)
        LocalDate checkOutDate,

        @Max(value = 10, message = Constants.GUESTS_COUNT_EXCEEDS_MAX)
        Integer guestsCount,

        @NotBlank(message = Constants.REQUIRED_FIELD_MISSING)
        String guestName,

        @NotBlank(message = Constants.REQUIRED_FIELD_MISSING)
        @Email(message = Constants.INVALID_EMAIL_FORMAT)
        String guestEmail,

        @NotBlank(message = Constants.REQUIRED_FIELD_MISSING)
        String guestPhone
) {
}