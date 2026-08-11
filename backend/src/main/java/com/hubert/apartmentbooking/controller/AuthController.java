package com.hubert.apartmentbooking.controller;

import com.hubert.apartmentbooking.constants.Constants;
import com.hubert.apartmentbooking.dto.request.LoginRequest;
import com.hubert.apartmentbooking.dto.request.RegisterRequest;
import com.hubert.apartmentbooking.dto.response.AuthResponse;
import com.hubert.apartmentbooking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Constants.AUTH_PATH)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(Constants.REGISTER_ENDPOINT)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping(Constants.LOGIN_ENDPOINT)
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}