package com.petcare.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcare.common.api.ApiResponse;
import com.petcare.common.exception.ErrorCode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeErrorResponse(
                                        response,
                                        objectMapper,
                                        HttpStatus.UNAUTHORIZED,
                                        ErrorCode.UNAUTHORIZED,
                                        "请先登录"
                                ))
                        .accessDeniedHandler((request, response, exception) ->
                                writeErrorResponse(
                                        response,
                                        objectMapper,
                                        HttpStatus.FORBIDDEN,
                                        ErrorCode.FORBIDDEN,
                                        "无权访问该资源"
                                )))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/system/health").permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }

    /**
     * Prevents Spring Boot from creating and logging a generated default user password.
     * Phase 4 replaces this placeholder with the real database-backed authentication service.
     */
    @Bean
    public UserDetailsService disabledUserDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Authentication is not enabled");
        };
    }

    private void writeErrorResponse(
            jakarta.servlet.http.HttpServletResponse response,
            ObjectMapper objectMapper,
            HttpStatus status,
            String code,
            String message
    ) throws java.io.IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(code, message));
    }
}
