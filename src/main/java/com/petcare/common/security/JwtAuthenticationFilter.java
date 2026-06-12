package com.petcare.common.security;

import com.petcare.admin.security.AdminUserDetailsService;
import com.petcare.user.security.UserAuthLoadingService;
import com.petcare.user.security.UserPrincipal;
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
 * Routes by tokenType:
 * - ADMIN -> AdminUserDetailsService -> AdminPrincipal
 * - USER  -> UserAuthLoadingService  -> UserPrincipal
 * - other -> no SecurityContext set, subsequent 401
 *
 * Behavior:
 * - Only processes Authorization: Bearer <token>
 * - Token missing: does not error, lets Spring Security decide if auth is required
 * - Token invalid/expired: clears context, lets entry point return 401
 * - Token valid: sets authentication in SecurityContext
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final AdminUserDetailsService adminUserDetailsService;
    private final UserAuthLoadingService userAuthLoadingService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                   AdminUserDetailsService adminUserDetailsService,
                                   UserAuthLoadingService userAuthLoadingService) {
        this.jwtTokenService = jwtTokenService;
        this.adminUserDetailsService = adminUserDetailsService;
        this.userAuthLoadingService = userAuthLoadingService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            String tokenType = jwtTokenService.getTokenType(token);

            switch (tokenType) {
                case "ADMIN" -> authenticateAdmin(token, request);
                case "USER" -> authenticateUser(token, request);
                default -> {
                    // Unknown tokenType — do not set SecurityContext
                }
            }
        } catch (JwtException | IllegalArgumentException e) {
            // Invalid or expired token — clear context and let entry point return 401
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateAdmin(String token, HttpServletRequest request) {
        Long adminId = jwtTokenService.getSubjectId(token);
        UserDetails userDetails = adminUserDetailsService.loadUserByAdminId(adminId);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void authenticateUser(String token, HttpServletRequest request) {
        Long userId = jwtTokenService.getSubjectId(token);
        UserPrincipal userPrincipal = userAuthLoadingService.loadActiveUserById(userId);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userPrincipal, null, userPrincipal.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
