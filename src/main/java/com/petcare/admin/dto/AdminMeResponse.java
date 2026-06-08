package com.petcare.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Current admin info response DTO.
 * Returned by GET /api/v1/admin/auth/me.
 * Never includes password.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminMeResponse(
        Long id,
        String username,
        String nickname,
        String role,
        List<String> permissions
) {
}
