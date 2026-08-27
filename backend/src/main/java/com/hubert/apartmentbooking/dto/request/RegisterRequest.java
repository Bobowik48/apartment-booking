package com.hubert.apartmentbooking.dto.request;

import com.hubert.apartmentbooking.constants.Constants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @NotBlank(message = Constants.REQUIRED_FIELD_MISSING)
        String fullName,

        @NotBlank(message = Constants.REQUIRED_FIELD_MISSING)
        @Email(message = Constants.INVALID_EMAIL_FORMAT)
        String email,

        @NotBlank(message = Constants.REQUIRED_FIELD_MISSING)
        @Pattern(regexp = "^[+]?[0-9 ]{7,15}$", message = Constants.INVALID_PHONE_FORMAT)
        String phone,

        @NotBlank(message = Constants.REQUIRED_FIELD_MISSING)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{6,}$",
                message = Constants.WEAK_PASSWORD
        )
        String password,

        @NotBlank(message = Constants.INVALID_CAPTCHA)
        String captchaToken
) {
}