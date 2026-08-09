package com.hubert.apartmentbooking.dto.response;

import java.time.LocalDate;
import java.util.List;

public record AvailabilityResponse(
        List<LocalDate> unavailableDates
) {
}