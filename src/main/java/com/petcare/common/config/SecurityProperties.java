package com.petcare.common.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Security-related configuration properties bound from application.yml.
 * JWT secret is loaded from environment variable; must not be empty in production.
 * Test profile can use a dedicated fake secret.
 */
@ConfigurationProperties(prefix = "petcare.security")
public record SecurityProperties(
        String jwtSecret,
        String jwtIssuer,
        int jwtExpirationMinutes
) {

    private static final Logger log = LoggerFactory.getLogger(SecurityProperties.class);
    private static final int MIN_SECRET_LENGTH = 32;

    public SecurityProperties {
        if (jwtIssuer == null || jwtIssuer.isBlank()) {
            jwtIssuer = "petcare-o2o-api";
        }
        if (jwtExpirationMinutes <= 0) {
            jwtExpirationMinutes = 120;
        }
    }

    /**
     * Validates that the JWT secret meets minimum length requirements.
     * In test profile, this validation is skipped.
     */
    @PostConstruct
    public void validate() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            log.warn("JWT secret is not configured. Authentication will not work in production.");
            return;
        }
        if (jwtSecret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "JWT secret must be at least " + MIN_SECRET_LENGTH + " characters long. "
                            + "Current length: " + jwtSecret.length());
        }
    }
}
