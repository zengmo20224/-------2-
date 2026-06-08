package com.petcare.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Security-related configuration properties bound from application.yml.
 * JWT secret is loaded from environment variable; must not be empty in production.
 */
@ConfigurationProperties(prefix = "petcare.security")
public record SecurityProperties(String jwtSecret) {
}
