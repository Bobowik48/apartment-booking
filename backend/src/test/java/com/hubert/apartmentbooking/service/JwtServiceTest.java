package com.hubert.apartmentbooking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JwtService}. The service reads its secret/expiration via {@code @Value}
 * field injection (no constructor), so this test sets those private fields directly with plain
 * reflection instead of pulling in a Spring context — keeping the test fast and dependency-free.
 */
class JwtServiceTest {

    // 256+ bits, as required by the HS256 algorithm jjwt uses under the hood.
    private static final String SECRET = "this-is-a-very-long-test-only-jwt-secret-value-1234567890";
    private static final long EXPIRATION_MS = 60 * 60 * 1000L; // 1 hour

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();
        setField(jwtService, "secret", SECRET);
        setField(jwtService, "expirationMs", EXPIRATION_MS);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void generateToken_producesATokenThatExtractsTheSameEmail() {
        String token = jwtService.generateToken("jan@example.com", "USER");

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractEmail(token)).isEqualTo("jan@example.com");
    }

    @Test
    void isTokenValid_returnsTrue_forAFreshlyGeneratedToken() {
        String token = jwtService.generateToken("jan@example.com", "USER");

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_forAGarbageToken() {
        assertThat(jwtService.isTokenValid("not-a-real-jwt")).isFalse();
    }

    @Test
    void isTokenValid_returnsFalse_whenSignedWithADifferentSecret() throws Exception {
        String token = jwtService.generateToken("jan@example.com", "USER");

        JwtService otherService = new JwtService();
        setField(otherService, "secret", "a-completely-different-secret-value-1234567890abcdef");
        setField(otherService, "expirationMs", EXPIRATION_MS);

        assertThat(otherService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_returnsFalse_forAnExpiredToken() throws Exception {
        JwtService expiringService = new JwtService();
        setField(expiringService, "secret", SECRET);
        setField(expiringService, "expirationMs", -1000L); // already expired the moment it's issued

        String expiredToken = expiringService.generateToken("jan@example.com", "USER");

        assertThat(jwtService.isTokenValid(expiredToken)).isFalse();
    }

    @Test
    void extractEmail_throws_forAnInvalidToken() {
        assertThatThrownBy(() -> jwtService.extractEmail("not-a-real-jwt"))
                .isInstanceOf(RuntimeException.class);
    }
}
