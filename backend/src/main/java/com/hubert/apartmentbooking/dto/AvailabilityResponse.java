package com.hubert.apartmentbooking.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class AvailabilityResponse {

    private List<LocalDate> unavailableDates;

    public AvailabilityResponse(List<LocalDate> unavailableDates) {
        this.unavailableDates = unavailableDates;
    }
}