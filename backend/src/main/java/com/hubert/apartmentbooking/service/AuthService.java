package com.hubert.apartmentbooking.service;

import com.hubert.apartmentbooking.constants.Constants;
import com.hubert.apartmentbooking.dto.request.LoginRequest;
import com.hubert.apartmentbooking.dto.request.RegisterRequest;
import com.hubert.apartmentbooking.dto.response.AuthResponse;
import com.hubert.apartmentbooking.exception.EmailAlreadyInUseException;
import com.hubert.apartmentbooking.exception.InvalidCaptchaException;
import com.hubert.apartmentbooking.exception.InvalidCredentialsException;
import com.hubert.apartmentbooking.model.User;
import com.hubert.apartmentbooking.model.enums.Role;
import com.hubert.apartmentbooking.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CaptchaService captchaService;


    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, CaptchaService captchaService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.captchaService = captchaService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (!captchaService.verify(request.captchaToken())) {
            throw new InvalidCaptchaException(Constants.INVALID_CAPTCHA);
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyInUseException(String.format(Constants.EMAIL_ALREADY_IN_USE, request.email()));
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getEmail(), saved.getRole().name());

        return new AuthResponse(saved.getId(), saved.getEmail(), saved.getRole(), token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException(Constants.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException(Constants.INVALID_CREDENTIALS);
        }
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(user.getId(), user.getEmail(), user.getRole(), token);
    }
}