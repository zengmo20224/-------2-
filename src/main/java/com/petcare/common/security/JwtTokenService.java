package com.petcare.common.security;

import com.petcare.common.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT token service for signing and parsing admin access tokens.
 *
 * JWT Claims:
 * - sub = adminId
 * - username = admin username
 * - role = admin_user.role
 * - tokenType = ADMIN
 * - iss = petcare-o2o-api
 * - iat = issued time
 * - exp = expiration time
 *
 * Prohibited from containing: password hash, phone, full permission list, API key, DB connection info.
 */
@Service
public class JwtTokenService {

    private final SecretKey signingKey;
    private final String issuer;
    private final int expirationMinutes;

    public JwtTokenService(SecurityProperties securityProperties) {
        this.signingKey = Keys.hmacShaKeyFor(
                securityProperties.jwtSecret().getBytes(StandardCharsets.UTF_8));
        this.issuer = securityProperties.jwtIssuer();
        this.expirationMinutes = securityProperties.jwtExpirationMinutes();
    }

    /**
     * Signs a new admin access token using the configured expiration.
     *
     * @param adminId  the admin user's ID
     * @param username the admin's username
     * @param role     the admin's role code (e.g. SUPER_ADMIN, MANAGER, STAFF)
     * @return signed JWT string
     */
    public String signAdminToken(Long adminId, String username, String role) {
        return signAdminToken(adminId, username, role, getExpirationSeconds());
    }

    /**
     * Signs a new admin access token with a custom expiration in seconds.
     * Used internally and for testing.
     *
     * @param adminId           the admin user's ID
     * @param username          the admin's username
     * @param role              the admin's role code
     * @param expirationSeconds custom expiration in seconds from now
     * @return signed JWT string
     */
    public String signAdminToken(Long adminId, String username, String role, int expirationSeconds) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .subject(String.valueOf(adminId))
                .claim("username", username)
                .claim("role", role)
                .claim("tokenType", "ADMIN")
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Parses and validates a JWT token.
     *
     * @param token the JWT string
     * @return parsed claims
     * @throws JwtException if the token is invalid, expired, or has wrong signature
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the admin ID from the token subject.
     *
     * @param token the JWT string
     * @return admin ID
     */
    public Long getAdminId(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Extracts the username from the token.
     *
     * @param token the JWT string
     * @return username
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * Extracts the role from the token.
     *
     * @param token the JWT string
     * @return role code
     */
    public String getRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * Returns the configured expiration duration in seconds.
     *
     * @return expiration in seconds
     */
    public int getExpirationSeconds() {
        return expirationMinutes * 60;
    }

    // --- USER token stubs (RED-1: will be implemented in GREEN-1) ---

    /**
     * Signs a new USER access token.
     * Stub — throws UnsupportedOperationException until GREEN-1.
     */
    public String signUserToken(Long userId) {
        throw new UnsupportedOperationException("signUserToken not yet implemented");
    }

    /**
     * Signs a new USER access token with custom expiration in seconds.
     * Stub — throws UnsupportedOperationException until GREEN-1.
     */
    public String signUserToken(Long userId, int expirationSeconds) {
        throw new UnsupportedOperationException("signUserToken(expiration) not yet implemented");
    }

    /**
     * Extracts the user ID from a USER token.
     * Must reject ADMIN tokens.
     * Stub — throws UnsupportedOperationException until GREEN-1.
     */
    public Long getUserId(String token) {
        throw new UnsupportedOperationException("getUserId not yet implemented");
    }

    /**
     * Extracts the subject ID from any valid token (admin or user).
     * Stub — throws UnsupportedOperationException until GREEN-1.
     */
    public Long getSubjectId(String token) {
        throw new UnsupportedOperationException("getSubjectId not yet implemented");
    }

    /**
     * Extracts the tokenType claim from a token.
     * Stub — throws UnsupportedOperationException until GREEN-1.
     */
    public String getTokenType(String token) {
        throw new UnsupportedOperationException("getTokenType not yet implemented");
    }
}
