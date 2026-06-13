package com.petcare.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response for login and register endpoints.
 * ID serialized as String for snowflake precision.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PasswordLoginResponse(
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
