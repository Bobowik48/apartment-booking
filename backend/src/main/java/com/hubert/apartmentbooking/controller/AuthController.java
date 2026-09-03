package com.hubert.apartmentbooking.controller;

import com.hubert.apartmentbooking.constants.Constants;
import com.hubert.apartmentbooking.dto.request.ForgotPasswordRequest;
import com.hubert.apartmentbooking.dto.request.LoginRequest;
import com.hubert.apartmentbooking.dto.request.RegisterRequest;
import com.hubert.apartmentbooking.dto.request.ResetPasswordRequest;
import com.hubert.apartmentbooking.dto.response.AuthResponse;
import com.hubert.apartmentbooking.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Constants.AUTH_PATH)
@Tag(name = "Auth", description = "Registration, login and password reset")
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

    @PostMapping(Constants.FORGOT_PASSWORD_ENDPOINT)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
    }

    @PostMapping(Constants.RESET_PASSWORD_ENDPOINT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
    }
}