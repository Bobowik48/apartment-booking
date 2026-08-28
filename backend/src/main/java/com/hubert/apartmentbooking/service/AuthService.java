package com.hubert.apartmentbooking.service;

import com.hubert.apartmentbooking.constants.Constants;
import com.hubert.apartmentbooking.dto.request.ForgotPasswordRequest;
import com.hubert.apartmentbooking.dto.request.LoginRequest;
import com.hubert.apartmentbooking.dto.request.RegisterRequest;
import com.hubert.apartmentbooking.dto.request.ResetPasswordRequest;
import com.hubert.apartmentbooking.dto.response.AuthResponse;
import com.hubert.apartmentbooking.exception.*;
import com.hubert.apartmentbooking.model.PasswordResetToken;
import com.hubert.apartmentbooking.model.User;
import com.hubert.apartmentbooking.model.enums.Role;
import com.hubert.apartmentbooking.repository.PasswordResetTokenRepository;
import com.hubert.apartmentbooking.repository.UserRepository;
import com.hubert.apartmentbooking.util.EmailTemplates;
import com.hubert.apartmentbooking.util.EmailTexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CaptchaService captchaService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final String frontendUrl;
    private final long resetTokenCooldownMinutes;
    private final long resetTokenValidityHours;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       CaptchaService captchaService, PasswordResetTokenRepository passwordResetTokenRepository,
                       EmailService emailService,
                       @Value("${app.frontend.url}") String frontendUrl,
                       @Value("${app.reset-token.cooldown-minutes}") long resetTokenCooldownMinutes,
                       @Value("${app.reset-token.validity-hours}") long resetTokenValidityHours) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.captchaService = captchaService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
        this.resetTokenCooldownMinutes = resetTokenCooldownMinutes;
        this.resetTokenValidityHours = resetTokenValidityHours;
    }

    public AuthResponse register(RegisterRequest request) {
        if (!captchaService.verify(request.captchaToken())) {
            throw new InvalidCaptchaException(Constants.INVALID_CAPTCHA);
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyInUseException(Constants.EMAIL_ALREADY_IN_USE);
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

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.email());
        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();
        Optional<PasswordResetToken> lastToken = passwordResetTokenRepository.findTopByUserOrderByCreatedAtDesc(user);
        boolean withinCooldown = lastToken.isPresent()
                && lastToken.get().getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(resetTokenCooldownMinutes));
        if (withinCooldown) {
            return;
        }
        passwordResetTokenRepository.deleteByUser(user);
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setCreatedAt(LocalDateTime.now());
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(resetTokenValidityHours));
        passwordResetTokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String html = buildResetPasswordEmail(user, resetLink);
        emailService.send(user.getEmail(), EmailTexts.RESET_PASSWORD_SUBJECT, html);
    }

    private String buildResetPasswordEmail(User user, String resetLink) {
        return EmailTemplates.button(
                EmailTexts.RESET_PASSWORD_BUTTON_TEXT,
                "<p style=\"margin:0 0 12px;\">" + EmailTexts.RESET_PASSWORD_GREETING.formatted(user.getFullName()) + "</p>"
                        + "<p style=\"margin:0;\">" + EmailTexts.RESET_PASSWORD_INTRO + "</p>",
                EmailTexts.RESET_PASSWORD_BUTTON_TEXT,
                resetLink,
                EmailTexts.RESET_PASSWORD_FOOTER
        );
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new PasswordMismatchException(Constants.PASSWORDS_DO_NOT_MATCH);
        }
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new InvalidResetTokenException(Constants.INVALID_RESET_TOKEN));
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidResetTokenException(Constants.RESET_TOKEN_EXPIRED);
        }
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
    }
}