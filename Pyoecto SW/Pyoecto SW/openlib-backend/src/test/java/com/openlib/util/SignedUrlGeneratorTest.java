package com.openlib.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SignedUrlGenerator — Unit Tests")
class SignedUrlGeneratorTest {

    private final SignedUrlGenerator generator =
            new SignedUrlGenerator(
                "testSecretKey1234567890abcdefghijklmnopqrstuvwxyz1234",
                5L
            );

    @Test
    @DisplayName("generateToken() — token válido recién generado")
    void token_isValid() {
        String token = generator.generateToken(1L, 42L);
        assertThat(generator.isValid(token)).isTrue();
    }

    @Test
    @DisplayName("isValid() — token manipulado es inválido")
    void tampered_token_isInvalid() {
        String token   = generator.generateToken(1L, 42L);
        String tampered = token.substring(0, token.length() - 4) + "xxxx";
        assertThat(generator.isValid(tampered)).isFalse();
    }

    @Test
    @DisplayName("isValid() — token vacío es inválido")
    void empty_token_isInvalid() {
        assertThat(generator.isValid("")).isFalse();
        assertThat(generator.isValid("invalid.token")).isFalse();
    }

    @Test
    @DisplayName("getExpiration() — devuelve fecha futura")
    void expiration_isFuture() {
        assertThat(generator.getExpiration())
                .isAfter(java.time.LocalDateTime.now());
    }
}
