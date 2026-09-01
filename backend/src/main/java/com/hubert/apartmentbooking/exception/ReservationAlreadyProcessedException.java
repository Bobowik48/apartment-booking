package com.hubert.apartmentbooking.exception;

public class ReservationAlreadyProcessedException extends RuntimeException {
    public ReservationAlreadyProcessedException(String message) {
        super(message);
    }
}
