package com.hubert.apartmentbooking.dto.response;

import com.hubert.apartmentbooking.model.Apartment;

import java.math.BigDecimal;

public record ApartmentResponse(
        Long id,
        String name,
        String description,
        String street,
        String apartmentNumber,
        String district,
        String city,
        BigDecimal pricePerNight,
        Integer maxGuests,
        BigDecimal area,
        Integer floor
) {
    public static ApartmentResponse from(Apartment apartment) {
        return new ApartmentResponse(
                apartment.getId(),
                apartment.getName(),
                apartment.getDescription(),
                apartment.getStreet(),
                apartment.getApartmentNumber(),
                apartment.getDistrict(),
                apartment.getCity(),
                apartment.getPricePerNight(),
                apartment.getMaxGuests(),
                apartment.getArea(),
                apartment.getFloor()
        );
    }
}