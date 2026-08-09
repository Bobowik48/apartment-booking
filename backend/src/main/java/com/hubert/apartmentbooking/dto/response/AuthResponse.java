package com.hubert.apartmentbooking.dto.response;

import com.hubert.apartmentbooking.model.enums.Role;

public record AuthResponse(
        Long id,
        String email,
        Role role,
        String token
) {
}