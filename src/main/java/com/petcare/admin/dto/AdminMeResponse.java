package com.petcare.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.util.List;

/**
 * Current admin info response DTO.
 * Returned by GET /api/v1/admin/auth/me.
 * Never includes password.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminMeResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        String username,
        String nickname,
        String role,
        List<String> permissions
) {
}
