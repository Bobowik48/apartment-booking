package com.hubert.apartmentbooking.dto.request;

import com.hubert.apartmentbooking.constants.Constants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = Constants.REQUIRED_FIELD_MISSING)
        @Email(message = Constants.INVALID_EMAIL_FORMAT)
        String email
) {
}