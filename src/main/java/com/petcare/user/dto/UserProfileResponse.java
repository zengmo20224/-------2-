package com.petcare.user.dto;

import com.petcare.user.entity.User;

import java.time.LocalDateTime;

/**
 * Response DTO for current user profile.
 * Exposes only safe, non-sensitive fields.
 * All fields are always present; avatarUrl may be null.
 */
public record UserProfileResponse(
        String userId,
        String nickname,
        String phone,
        String avatarUrl,
        String realName,
        String idCardNo,
        String idCardImageUrl,
        LocalDateTime createdAt
) {
    /**
     * Creates a UserProfileResponse from a User entity.
     * Phone is masked; sensitive fields (openid, unionid, status, deleted) are excluded.
     */
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                String.valueOf(user.getId()),
                user.getNickname(),
                maskPhone(user.getPhone()),
                user.getAvatarUrl(),
                user.getRealName(),
                user.getIdCardNo(),
                user.getIdCardImageUrl(),
                user.getCreateTime()
        );
    }

    /**
     * Masks a phone number: 13800138001 -> 138****8001.
     * Only accepts valid Chinese mainland format ^1\\d{10}$.
     * Returns null for null, blank, wrong length, or non-digit values.
     */
    static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        if (!phone.matches("^1\\d{10}$")) {
            return null;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
