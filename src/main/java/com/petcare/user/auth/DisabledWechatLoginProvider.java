package com.petcare.user.auth;

import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

/**
 * Disabled WeChat login provider for V1.
 * Always throws an exception indicating WeChat login is not enabled.
 * Does NOT create users, generate openid, or issue tokens.
 */
@Service
public class DisabledWechatLoginProvider implements WechatLoginProvider {

    @Override
    public WechatLoginResult login(String code) {
        throw new BusinessException(ErrorCode.WECHAT_LOGIN_NOT_ENABLED, "微信登录暂未启用");
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
