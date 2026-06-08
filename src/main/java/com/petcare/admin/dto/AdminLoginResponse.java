package com.petcare.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Admin login success response DTO.
 * Never includes password.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminLoginResponse(
        String tokenType,
        String accessToken,
        int expiresInSeconds,
        AdminSummary admin
) {

    /**
     * Admin summary included in login response.
     */
    public record AdminSummary(
            Long id,
            String username,
            String nickname,
            String role
    ) {
    }
}
