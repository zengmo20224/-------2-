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

    // === USER JWT Contract Tests (RED-1: Phase 11-01) ===

    @Test
    @DisplayName("USER token should contain correct sub and tokenType=USER")
    void userTokenShouldContainCorrectClaims() {
        Long userId = 3001L;
        String token = jwtTokenService.signUserToken(userId);
        assertThat(token).isNotBlank();

        Claims claims = jwtTokenService.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo(String.valueOf(userId));
        assertThat(claims.get("tokenType", String.class)).isEqualTo("USER");
        assertThat(claims.getIssuer()).isEqualTo("petcare-o2o-api-test");
    }

    @Test
    @DisplayName("USER token must not contain phone, openid, unionid, admin role or permissions")
    void userTokenMustNotContainPrivateFields() {
        String token = jwtTokenService.signUserToken(5001L);

        Claims claims = jwtTokenService.parseToken(token);
        assertThat(claims.get("phone")).isNull();
        assertThat(claims.get("openid")).isNull();
        assertThat(claims.get("unionid")).isNull();
        assertThat(claims.get("username")).isNull();
        assertThat(claims.get("role")).isNull();
        assertThat(claims.get("permissions")).isNull();
    }

    @Test
    @DisplayName("getUserId should parse USER token and return user ID")
    void getUserIdShouldParseUserToken() {
        Long userId = 4001L;
        String token = jwtTokenService.signUserToken(userId);

        Long parsedId = jwtTokenService.getUserId(token);
        assertThat(parsedId).isEqualTo(userId);
    }

    @Test
    @DisplayName("getUserId must reject ADMIN token")
    void getUserIdMustRejectAdminToken() {
        String adminToken = jwtTokenService.signAdminToken(1L, "admin", "SUPER_ADMIN");

        assertThatThrownBy(() -> jwtTokenService.getUserId(adminToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("getAdminId must reject USER token")
    void getAdminIdMustRejectUserToken() {
        String userToken = jwtTokenService.signUserToken(5001L);

        assertThatThrownBy(() -> jwtTokenService.getAdminId(userToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Unknown or missing tokenType must be rejected by getUserId")
    void unknownTokenTypeMustBeRejected() {
        // Create a token manually with no tokenType claim
        io.jsonwebtoken.JwtBuilder builder = io.jsonwebtoken.Jwts.builder()
                .subject("9999")
                .issuer("petcare-o2o-api-test")
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 3600000));

        String noTypeToken = builder.signWith(
                io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        "test-secret-key-for-unit-tests-at-least-32-chars".getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();

        assertThatThrownBy(() -> jwtTokenService.getUserId(noTypeToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("getTokenType should return correct type for both token kinds")
    void getTokenTypeShouldReturnCorrectType() {
        String adminToken = jwtTokenService.signAdminToken(1L, "admin", "SUPER_ADMIN");
        assertThat(jwtTokenService.getTokenType(adminToken)).isEqualTo("ADMIN");

        String userToken = jwtTokenService.signUserToken(5001L);
        assertThat(jwtTokenService.getTokenType(userToken)).isEqualTo("USER");
    }

    @Test
    @DisplayName("Expired USER token must be rejected")
    void expiredUserTokenMustBeRejected() {
        // Sign a USER token that expired 60 seconds ago
        // signUserToken with custom expiration not available yet — will add in GREEN
        // For now, verify the contract by using the general parseToken rejection
        String token = jwtTokenService.signUserToken(1L, -60);
        assertThatThrownBy(() -> jwtTokenService.parseToken(token))
                .isInstanceOf(Exception.class);
    }
}
