package com.hubert.apartmentbooking.exception;

public class DatesNotAvailableException extends RuntimeException {
  public DatesNotAvailableException(String message) {
    super(message);
  }
}