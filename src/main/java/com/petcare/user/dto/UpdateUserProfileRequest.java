package com.petcare.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating current user profile.
 * Only nickname and avatarUrl are allowed.
 */
public record UpdateUserProfileRequest(
        @NotBlank(message = "昵称不能为空")
        @Size(max = 64, message = "昵称最长64个字符")
        String nickname,

        @Size(max = 255, message = "头像URL最长255个字符")
        String avatarUrl
) {
}
