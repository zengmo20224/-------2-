package com.petcare.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating current user profile.
 * Allows nickname, avatarUrl, real name and ID card info.
 */
public record UpdateUserProfileRequest(
        @NotBlank(message = "昵称不能为空")
        @Size(max = 64, message = "昵称最长64个字符")
        String nickname,

        @Size(max = 255, message = "头像URL最长255个字符")
        String avatarUrl,

        @Size(max = 64, message = "真实姓名最长64个字符")
        String realName,

        @Size(max = 18, message = "身份证号最长18个字符")
        String idCardNo,

        @Size(max = 255, message = "身份证照片URL最长255个字符")
        String idCardImageUrl
) {
}
