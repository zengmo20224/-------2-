package com.petcare.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Minimal Spring Security skeleton.
 *
 * Phase 2 scope:
 * - Health check endpoint is publicly accessible.
 * - All other API endpoints require authentication (but no real auth is implemented yet).
 * - CSRF disabled because the API is stateless (JWT-based auth planned for phase 4).
 *
 * Phase 4 will add:
 * - JWT filter
 * - Role-based access control
 * - Permission code checks
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/system/health").permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }
}
