package com.hubert.apartmentbooking.constants;

public final class Constants {

    private Constants() {
    }

    // API paths
    public static final String AVAILABILITY_PATH = "/api/availability";
    public static final String RESERVATIONS_PATH = "/api/reservations";
    public static final String AUTH_PATH = "/api/auth";
    public static final String REGISTER_ENDPOINT = "/register";
    public static final String LOGIN_ENDPOINT = "/login";
    public static final String ADMIN_PATH = "/api/admin";

    // Error messages
    public static final String APARTMENT_NOT_FOUND = "Apartment not found: %s";
    public static final String DATES_NOT_AVAILABLE = "Selected dates are not available";
    public static final String EMAIL_ALREADY_IN_USE = "Email already in use: %s";
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String INVALID_EMAIL_FORMAT = "Invalid email format";
    public static final String WEAK_PASSWORD = "Password must be at least 6 characters long and contain an uppercase letter, a lowercase letter, and a special character";
}