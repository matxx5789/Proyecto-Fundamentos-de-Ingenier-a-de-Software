package com.openlib.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Generador de Signed URLs temporales para descargas seguras.
 *
 * Formato del token:  base64( userId:bookId:expiresEpoch:nonce ) + "." + hmac
 * El token expira en {@code expirationMinutes} y es único por usuario + libro + momento.
 */
@Slf4j
@Component
public class SignedUrlGenerator {

    private final String secret;
    private final long   expirationMinutes;

    public SignedUrlGenerator(
            @Value("${openlib.signed-url.secret}")             String secret,
            @Value("${openlib.signed-url.expiration-minutes}") long expirationMinutes) {
        this.secret            = secret;
        this.expirationMinutes = expirationMinutes;
    }

    /**
     * Genera un token firmado de descarga.
     */
    public String generateToken(Long userId, Long bookId) {
        long expiresAt = System.currentTimeMillis() + (expirationMinutes * 60_000L);
        String nonce   = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String payload = userId + ":" + bookId + ":" + expiresAt + ":" + nonce;
        String encoded = Base64.getUrlEncoder().withoutPadding()
                               .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String hmac    = hmacSha256(encoded);
        return encoded + "." + hmac;
    }

    /**
     * @return la fecha de expiración del token.
     */
    public LocalDateTime getExpiration() {
        return LocalDateTime.now().plusMinutes(expirationMinutes);
    }

    /**
     * Valida que el token es auténtico y no ha expirado.
     */
    public boolean isValid(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) return false;

            String expectedHmac = hmacSha256(parts[0]);
            if (!expectedHmac.equals(parts[1])) {
                log.warn("HMAC de token inválido");
                return false;
            }

            String decoded = new String(
                    Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String[] segments = decoded.split(":");
            long expiresAt = Long.parseLong(segments[2]);

            if (System.currentTimeMillis() > expiresAt) {
                log.info("Token de descarga expirado");
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Error validando signed token: {}", e.getMessage());
            return false;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String hmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(raw);
        } catch (Exception e) {
            throw new IllegalStateException("Error generando HMAC", e);
        }
    }
}
