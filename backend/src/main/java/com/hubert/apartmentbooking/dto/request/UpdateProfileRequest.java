package com.hubert.apartmentbooking.dto.request;

import com.hubert.apartmentbooking.constants.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdatePhoneRequest(
        @NotBlank(message = Constants.REQUIRED_FIELD_MISSING)
        @Pattern(regexp = "^[+]?[0-9 ]{7,15}$", message = Constants.INVALID_PHONE_FORMAT)
        String phone
) {
}