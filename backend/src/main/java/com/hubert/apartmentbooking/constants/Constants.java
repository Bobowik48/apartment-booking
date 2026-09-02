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
    public static final String FORGOT_PASSWORD_ENDPOINT = "/forgot-password";
    public static final String RESET_PASSWORD_ENDPOINT = "/reset-password";
    public static final String USERS_PATH = "/api/users";
    public static final String ME_ENDPOINT = "/me";
    public static final String PAYMENTS_PATH = "/api/payments";
    public static final String NOTIFY_ENDPOINT = "/notify";

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
    public static final String INVALID_PHONE_FORMAT = "INVALID_PHONE_FORMAT";
    public static final String INVALID_CAPTCHA = "INVALID_CAPTCHA";
    public static final String EMAIL_SEND_FAILED = "EMAIL_SEND_FAILED";
    public static final String INVALID_RESET_TOKEN = "INVALID_RESET_TOKEN";
    public static final String RESET_TOKEN_EXPIRED = "RESET_TOKEN_EXPIRED";
    public static final String PASSWORDS_DO_NOT_MATCH = "PASSWORDS_DO_NOT_MATCH";
    public static final String PAYU_ORDER_CREATION_FAILED = "PAYU_ORDER_CREATION_FAILED";
    public static final String INVALID_PAYU_SIGNATURE = "INVALID_PAYU_SIGNATURE";
    public static final String PAYMENT_NOT_FOUND = "PAYMENT_NOT_FOUND";
    public static final String RESERVATION_ALREADY_PROCESSED = "RESERVATION_ALREADY_PROCESSED";
    public static final String CURRENT_PASSWORD_INCORRECT = "CURRENT_PASSWORD_INCORRECT";
}