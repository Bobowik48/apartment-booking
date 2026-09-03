package com.hubert.apartmentbooking.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PayUSignatureVerifier}, which authenticates incoming PayU webhook
 * notifications by re-computing a hash of (body + secondKey) and comparing it against the
 * signature PayU sent in the {@code OpenPayu-Signature} header.
 */
class PayUSignatureVerifierTest {

    private static final String SECOND_KEY = "test-second-key";
    private static final String BODY = "{\"order\":{\"orderId\":\"ABC123\",\"status\":\"COMPLETED\"}}";

    private static String md5Hex(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Test
    void isValid_returnsTrue_forACorrectlySignedNotification() throws Exception {
        String signature = md5Hex(BODY + SECOND_KEY);
        String header = "signature=" + signature + ";algorithm=MD5";

        assertThat(PayUSignatureVerifier.isValid(BODY, header, SECOND_KEY)).isTrue();
    }

    @Test
    void isValid_isCaseInsensitiveOnTheHexSignature() throws Exception {
        String signature = md5Hex(BODY + SECOND_KEY).toUpperCase();
        String header = "signature=" + signature + ";algorithm=MD5";

        assertThat(PayUSignatureVerifier.isValid(BODY, header, SECOND_KEY)).isTrue();
    }

    @Test
    void isValid_returnsFalse_whenSignatureDoesNotMatch() {
        String header = "signature=0000000000000000000000000000000;algorithm=MD5";

        assertThat(PayUSignatureVerifier.isValid(BODY, header, SECOND_KEY)).isFalse();
    }

    @Test
    void isValid_returnsFalse_whenTheBodyWasTamperedWith() throws Exception {
        String signature = md5Hex(BODY + SECOND_KEY);
        String header = "signature=" + signature + ";algorithm=MD5";
        String tamperedBody = BODY.replace("COMPLETED", "CANCELED");

        assertThat(PayUSignatureVerifier.isValid(tamperedBody, header, SECOND_KEY)).isFalse();
    }

    @Test
    void isValid_returnsFalse_whenHeaderIsNull() {
        assertThat(PayUSignatureVerifier.isValid(BODY, null, SECOND_KEY)).isFalse();
    }

    @Test
    void isValid_returnsFalse_whenHeaderHasNoSignaturePart() {
        assertThat(PayUSignatureVerifier.isValid(BODY, "algorithm=MD5", SECOND_KEY)).isFalse();
    }

    @Test
    void isValid_supportsSha256Algorithm() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest((BODY + SECOND_KEY).getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        String header = "signature=" + sb + ";algorithm=SHA256";

        assertThat(PayUSignatureVerifier.isValid(BODY, header, SECOND_KEY)).isTrue();
    }
}
