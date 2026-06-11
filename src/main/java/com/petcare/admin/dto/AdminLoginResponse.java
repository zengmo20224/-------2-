package com.petcare.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

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
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
            String username,
            String nickname,
            String role
    ) {
    }
}
