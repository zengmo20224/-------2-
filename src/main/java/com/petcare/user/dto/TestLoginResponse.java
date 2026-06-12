package com.petcare.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response body for POST /api/v1/auth/test-login.
 * ID is serialized as String per project convention (snowflake precision).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TestLoginResponse(
        String tokenType,
        String accessToken,
        int expiresInSeconds,
        UserInfo user
) {
    public record UserInfo(
            String id,
            String nickname
    ) {}
}
