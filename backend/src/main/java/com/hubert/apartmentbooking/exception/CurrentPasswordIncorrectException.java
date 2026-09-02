package com.hubert.apartmentbooking.exception;

public class CurrentPasswordIncorrectException extends RuntimeException {
  public CurrentPasswordIncorrectException(String message) {
    super(message);
  }
}
