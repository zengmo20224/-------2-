package com.petcare.user.auth;

/**
 * Result from WeChat login processing.
 * In V1, this is only used as a placeholder.
 */
public record WechatLoginResult(
        String openid,
        String unionid,
        Long userId
) {
}
