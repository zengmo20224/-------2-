package com.petcare.user.auth;

/**
 * Interface for WeChat login provider.
 * V1 only provides a disabled implementation that returns "not enabled".
 * Real implementation will be added when WeChat mini-program integration is ready.
 */
public interface WechatLoginProvider {

    /**
     * Processes a WeChat login code.
     *
     * @param code the temporary authorization code from WeChat mini-program
     * @return WechatLoginResult containing user info (in real implementation)
     */
    WechatLoginResult login(String code);

    /**
     * Returns whether this provider is enabled.
     */
    boolean isEnabled();
}
