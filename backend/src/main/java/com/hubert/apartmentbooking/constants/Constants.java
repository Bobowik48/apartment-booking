package com.hubert.apartmentbooking.constants;

public final class Constants {

    private Constants() {
    }

    // API paths
    public static final String AVAILABILITY_PATH = "/api/availability";
    public static final String RESERVATIONS_PATH = "/api/reservations";

    // Error messages
    public static final String APARTMENT_NOT_FOUND = "Apartment not found: %s";
    public static final String DATES_NOT_AVAILABLE = "Selected dates are not available";
}