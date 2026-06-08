package com.petcare.common.security;

import com.petcare.common.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TDD RED phase: Tests for JwtTokenService.
 * Covers token signing, parsing, expiration, and invalid signature.
 */
class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        SecurityProperties properties = new SecurityProperties(
                "test-secret-key-for-unit-tests-at-least-32-chars",
                "petcare-o2o-api-test",
                120
        );
        jwtTokenService = new JwtTokenService(properties);
    }

    @Test
    @DisplayName("Should sign and parse admin token correctly")
    void shouldSignAndParseAdminToken() {
        Long adminId = 1001L;
        String username = "admin";
        String role = "SUPER_ADMIN";

        String token = jwtTokenService.signAdminToken(adminId, username, role);
        assertThat(token).isNotBlank();

        Claims claims = jwtTokenService.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo(String.valueOf(adminId));
        assertThat(claims.get("username", String.class)).isEqualTo(username);
        assertThat(claims.get("role", String.class)).isEqualTo(role);
        assertThat(claims.get("tokenType", String.class)).isEqualTo("ADMIN");
        assertThat(claims.getIssuer()).isEqualTo("petcare-o2o-api-test");
    }

    @Test
    @DisplayName("Should extract adminId from token subject")
    void shouldExtractAdminId() {
        String token = jwtTokenService.signAdminToken(2001L, "manager1", "MANAGER");

        Long adminId = jwtTokenService.getAdminId(token);
        assertThat(adminId).isEqualTo(2001L);
    }

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsername() {
        String token = jwtTokenService.signAdminToken(1L, "staff1", "STAFF");

        String username = jwtTokenService.getUsername(token);
        assertThat(username).isEqualTo("staff1");
    }

    @Test
    @DisplayName("Should extract role from token")
    void shouldExtractRole() {
        String token = jwtTokenService.signAdminToken(1L, "admin", "SUPER_ADMIN");

        String role = jwtTokenService.getRole(token);
        assertThat(role).isEqualTo("SUPER_ADMIN");
    }

    @Test
    @DisplayName("Should throw on expired token")
    void shouldThrowOnExpiredToken() {
        // Sign a token that expired 60 seconds ago (-60 seconds from now)
        String token = jwtTokenService.signAdminToken(1L, "admin", "SUPER_ADMIN", -60);

        assertThatThrownBy(() -> jwtTokenService.parseToken(token))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should throw on token with wrong secret")
    void shouldThrowOnWrongSecret() {
        String token = jwtTokenService.signAdminToken(1L, "admin", "SUPER_ADMIN");

        SecurityProperties otherProperties = new SecurityProperties(
                "another-secret-key-that-is-at-least-32-chars-long",
                "petcare-o2o-api-test",
                120
        );
        JwtTokenService otherService = new JwtTokenService(otherProperties);

        assertThatThrownBy(() -> otherService.parseToken(token))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should throw on malformed token")
    void shouldThrowOnMalformedToken() {
        assertThatThrownBy(() -> jwtTokenService.parseToken("not.a.valid-token"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should throw on empty token")
    void shouldThrowOnEmptyToken() {
        assertThatThrownBy(() -> jwtTokenService.parseToken(""))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should return correct expiration seconds")
    void shouldReturnCorrectExpirationSeconds() {
        int expirationSeconds = jwtTokenService.getExpirationSeconds();
        assertThat(expirationSeconds).isEqualTo(120 * 60);
    }
}
