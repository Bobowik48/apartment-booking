package com.hubert.apartmentbooking.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public final class PayUSignatureVerifier {

    private PayUSignatureVerifier() {
    }

    public static boolean isValid(String rawBody, String signatureHeader, String secondKey) {
        if (signatureHeader == null) return false;

        Map<String, String> parts = parseHeader(signatureHeader);
        String receivedSignature = parts.get("signature");
        String algorithm = parts.getOrDefault("algorithm", "MD5");

        if (receivedSignature == null) return false;

        String computedSignature = hash(rawBody + secondKey, toJavaAlgorithmName(algorithm));

        return MessageDigest.isEqual(
                computedSignature.toLowerCase().getBytes(StandardCharsets.UTF_8),
                receivedSignature.toLowerCase().getBytes(StandardCharsets.UTF_8));
    }

    private static Map<String, String> parseHeader(String header) {
        Map<String, String> result = new HashMap<>();
        for (String pair : header.split(";")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                result.put(kv[0].trim(), kv[1].trim());
            }
        }
        return result;
    }

    private static String toJavaAlgorithmName(String algorithm) {
        return switch (algorithm.toUpperCase()) {
            case "MD5" -> "MD5";
            case "SHA", "SHA1" -> "SHA-1";
            case "SHA256" -> "SHA-256";
            case "SHA384" -> "SHA-384";
            case "SHA512" -> "SHA-512";
            default -> "MD5";
        };
    }

    private static String hash(String input, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unsupported hash algorithm: " + algorithm, e);
        }
    }
}