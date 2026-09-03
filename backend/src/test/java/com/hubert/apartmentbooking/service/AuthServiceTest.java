package com.hubert.apartmentbooking.service;

import com.hubert.apartmentbooking.dto.request.ForgotPasswordRequest;
import com.hubert.apartmentbooking.dto.request.LoginRequest;
import com.hubert.apartmentbooking.dto.request.RegisterRequest;
import com.hubert.apartmentbooking.dto.request.ResetPasswordRequest;
import com.hubert.apartmentbooking.dto.response.AuthResponse;
import com.hubert.apartmentbooking.exception.EmailAlreadyInUseException;
import com.hubert.apartmentbooking.exception.InvalidCaptchaException;
import com.hubert.apartmentbooking.exception.InvalidCredentialsException;
import com.hubert.apartmentbooking.exception.InvalidResetTokenException;
import com.hubert.apartmentbooking.exception.PasswordMismatchException;
import com.hubert.apartmentbooking.model.PasswordResetToken;
import com.hubert.apartmentbooking.model.User;
import com.hubert.apartmentbooking.model.enums.Role;
import com.hubert.apartmentbooking.repository.PasswordResetTokenRepository;
import com.hubert.apartmentbooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthService}: registration, login, and the forgot/reset password flow.
 * JwtService, PasswordEncoder, CaptchaService, EmailService and the repositories are all mocked.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private CaptchaService captchaService;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private EmailService emailService;

    private AuthService authService;

    private static final String FRONTEND_URL = "http://localhost:4200";
    private static final long COOLDOWN_MINUTES = 2;
    private static final long VALIDITY_HOURS = 1;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService, captchaService,
                passwordResetTokenRepository, emailService, FRONTEND_URL, COOLDOWN_MINUTES, VALIDITY_HOURS);
    }

    // ### register ###

    @Test
    void register_createsUser_andReturnsTokenOnSuccess() {
        RegisterRequest request = new RegisterRequest("Jan Kowalski", "jan@example.com",
                "+48600000000", "Secret123!", "captcha-token");

        when(captchaService.verify("captcha-token")).thenReturn(true);
        when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Secret123!")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateToken("jan@example.com", "USER")).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("jan@example.com");
        assertThat(response.role()).isEqualTo(Role.USER);
        assertThat(response.token()).isEqualTo("jwt-token");

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertThat(savedUser.getValue().getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedUser.getValue().getRole()).isEqualTo(Role.USER);
    }

    @Test
    void register_throws_whenCaptchaInvalid() {
        RegisterRequest request = new RegisterRequest("Jan Kowalski", "jan@example.com",
                "+48600000000", "Secret123!", "bad-token");

        when(captchaService.verify("bad-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(request)).isInstanceOf(InvalidCaptchaException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_throws_whenEmailAlreadyRegistered() {
        RegisterRequest request = new RegisterRequest("Jan Kowalski", "jan@example.com",
                "+48600000000", "Secret123!", "captcha-token");

        when(captchaService.verify("captcha-token")).thenReturn(true);
        when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(request)).isInstanceOf(EmailAlreadyInUseException.class);

        verify(userRepository, never()).save(any());
    }

    // ### login ###

    @Test
    void login_returnsToken_whenCredentialsAreValid() {
        User user = new User();
        user.setId(5L);
        user.setEmail("jan@example.com");
        user.setPasswordHash("hashed-password");
        user.setRole(Role.USER);

        when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret123!", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken("jan@example.com", "USER")).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("jan@example.com", "Secret123!"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.id()).isEqualTo(5L);
    }

    @Test
    void login_throws_whenUserDoesNotExist() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("unknown@example.com", "whatever")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throws_whenPasswordDoesNotMatch() {
        User user = new User();
        user.setEmail("jan@example.com");
        user.setPasswordHash("hashed-password");

        when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("jan@example.com", "wrong-password")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    // ### forgotPassword ###

    @Test
    void forgotPassword_createsTokenAndSendsEmail_whenUserExistsAndNotInCooldown() {
        User user = new User();
        user.setEmail("jan@example.com");
        user.setFullName("Jan Kowalski");

        when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.empty());

        authService.forgotPassword(new ForgotPasswordRequest("jan@example.com"));

        verify(passwordResetTokenRepository).deleteByUser(user);
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailService, times(1)).send(eq("jan@example.com"), any(), any());
    }

    @Test
    void forgotPassword_doesNothing_whenUserDoesNotExist_toAvoidLeakingWhichEmailsAreRegistered() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword(new ForgotPasswordRequest("unknown@example.com"));

        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).send(any(), any(), any());
    }

    @Test
    void forgotPassword_doesNothing_whenAPreviousTokenWasIssuedWithinTheCooldownWindow() {
        User user = new User();
        user.setEmail("jan@example.com");

        PasswordResetToken recentToken = new PasswordResetToken();
        recentToken.setCreatedAt(LocalDateTime.now().minusSeconds(30)); // well within the 2-minute cooldown

        when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.of(recentToken));

        authService.forgotPassword(new ForgotPasswordRequest("jan@example.com"));

        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).send(any(), any(), any());
    }

    // ### resetPassword ###

    @Test
    void resetPassword_updatesPasswordAndDeletesToken_onSuccess() {
        User user = new User();
        user.setPasswordHash("old-hash");

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("reset-token");
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));

        when(passwordResetTokenRepository.findByToken("reset-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NewSecret123!")).thenReturn("new-hash");

        authService.resetPassword(new ResetPasswordRequest("reset-token", "NewSecret123!", "NewSecret123!"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).delete(token);
    }

    @Test
    void resetPassword_throws_whenPasswordsDoNotMatch() {
        assertThatThrownBy(() -> authService.resetPassword(
                new ResetPasswordRequest("reset-token", "Secret123!", "Different123!")))
                .isInstanceOf(PasswordMismatchException.class);

        verify(passwordResetTokenRepository, never()).findByToken(any());
    }

    @Test
    void resetPassword_throws_whenTokenUnknown() {
        when(passwordResetTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(
                new ResetPasswordRequest("missing", "Secret123!", "Secret123!")))
                .isInstanceOf(InvalidResetTokenException.class);
    }

    @Test
    void resetPassword_throwsAndDeletesToken_whenTokenExpired() {
        PasswordResetToken expiredToken = new PasswordResetToken();
        expiredToken.setToken("expired-token");
        expiredToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> authService.resetPassword(
                new ResetPasswordRequest("expired-token", "Secret123!", "Secret123!")))
                .isInstanceOf(InvalidResetTokenException.class);

        verify(passwordResetTokenRepository).delete(expiredToken);
        verify(userRepository, never()).save(any());
    }
}
