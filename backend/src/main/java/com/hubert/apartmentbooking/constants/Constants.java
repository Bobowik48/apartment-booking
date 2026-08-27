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
    public static final String APARTMENTS_PATH = "/api/apartments";
    public static final String MY_RESERVATIONS_PATH = "/my";

    // Error codes
    public static final String APARTMENT_NOT_FOUND = "APARTMENT_NOT_FOUND";
    public static final String DATES_NOT_AVAILABLE = "DATES_NOT_AVAILABLE";
    public static final String EMAIL_ALREADY_IN_USE = "EMAIL_ALREADY_IN_USE";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String INVALID_EMAIL_FORMAT = "INVALID_EMAIL_FORMAT";
    public static final String WEAK_PASSWORD = "WEAK_PASSWORD";
    public static final String CHECK_OUT_BEFORE_CHECK_IN = "CHECK_OUT_BEFORE_CHECK_IN";
    public static final String REQUIRED_FIELD_MISSING = "REQUIRED_FIELD_MISSING";
    public static final String GUESTS_COUNT_EXCEEDS_MAX = "GUESTS_COUNT_EXCEEDS_MAX";
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String RESERVATION_NOT_FOUND = "RESERVATION_NOT_FOUND";
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String FILE_TOO_LARGE = "FILE_TOO_LARGE";
    public static final String INVALID_PHONE_FORMAT = "INVALID_PHONE_FORMAT";
    public static final String INVALID_CAPTCHA = "INVALID_CAPTCHA";
}