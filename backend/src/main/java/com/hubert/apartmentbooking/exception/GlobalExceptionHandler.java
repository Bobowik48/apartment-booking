package com.hubert.apartmentbooking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;

import static com.hubert.apartmentbooking.constants.Constants.VALIDATION_FAILED;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApartmentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleApartmentNotFound(ApartmentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("errorCode", ex.getMessage()));
    }

    @ExceptionHandler(DatesNotAvailableException.class)
    public ResponseEntity<Map<String, String>> handleDatesNotAvailable(DatesNotAvailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("errorCode", ex.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("errorCode", ex.getMessage()));
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyInUse(EmailAlreadyInUseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("errorCode", ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("errorCode", ex.getMessage()));
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<Map<String, String>> handleInvalidDateRange(InvalidDateRangeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errorCode", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String errorCode = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(VALIDATION_FAILED);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errorCode", errorCode));
    }
}