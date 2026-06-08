package com.petcare.common.security;

import com.petcare.admin.security.AdminUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter that extracts Bearer tokens from Authorization header.
 *
 * Behavior:
 * - Only processes Authorization: Bearer <token>
 * - Token missing: does not error, lets Spring Security decide if auth is required
 * - Token invalid/expired: returns 401
 * - Token valid: sets authentication in SecurityContext
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final AdminUserDetailsService adminUserDetailsService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                   AdminUserDetailsService adminUserDetailsService) {
        this.jwtTokenService = jwtTokenService;
        this.adminUserDetailsService = adminUserDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            // No Bearer token — let Spring Security decide if auth is required
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            Long adminId = jwtTokenService.getAdminId(token);
            String tokenType = jwtTokenService.parseToken(token).get("tokenType", String.class);

            if (!"ADMIN".equals(tokenType)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Load fresh permissions from database
            UserDetails userDetails = adminUserDetailsService.loadUserByAdminId(adminId);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | IllegalArgumentException e) {
            // Invalid or expired token — clear context and let entry point return 401
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
