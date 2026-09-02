package com.hubert.apartmentbooking.dto.request;

import com.hubert.apartmentbooking.constants.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequest(
        @NotBlank(message = Constants.REQUIRED_FIELD_MISSING)
        String currentPassword,

        @NotBlank(message = Constants.REQUIRED_FIELD_MISSING)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{6,}$",
                message = Constants.WEAK_PASSWORD
        )
        String newPassword,

        @NotBlank(message = Constants.REQUIRED_FIELD_MISSING)
        String confirmPassword
) {
}