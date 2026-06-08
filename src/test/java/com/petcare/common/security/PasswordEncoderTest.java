package com.petcare.common.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for password encoding behavior.
 * Verifies BCrypt matching works and plaintext is not equal to hash.
 */
class PasswordEncoderTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Test
    @DisplayName("BCrypt should match encoded password")
    void shouldMatchEncodedPassword() {
        String rawPassword = "securePassword123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertThat(passwordEncoder.matches(rawPassword, encoded)).isTrue();
    }

    @Test
    @DisplayName("Plaintext should not equal encoded hash")
    void plaintextShouldNotEqualHash() {
        String rawPassword = "securePassword123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertThat(encoded).isNotEqualTo(rawPassword);
    }

    @Test
    @DisplayName("Wrong password should not match")
    void wrongPasswordShouldNotMatch() {
        String encoded = passwordEncoder.encode("correctPassword");

        assertThat(passwordEncoder.matches("wrongPassword", encoded)).isFalse();
    }

    @Test
    @DisplayName("Each encoding should produce different hash")
    void eachEncodingShouldProduceDifferentHash() {
        String rawPassword = "samePassword";
        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);

        assertThat(encoded1).isNotEqualTo(encoded2);
    }
}
