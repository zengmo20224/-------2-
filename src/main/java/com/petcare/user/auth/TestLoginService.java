package com.petcare.user.auth;

import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.common.security.JwtTokenService;
import com.petcare.user.dto.TestLoginResponse;
import com.petcare.user.entity.User;
import com.petcare.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Test-only login application service.
 * Only active when "test" profile is loaded.
 *
 * Enforces phone allowlist boundary:
 * - Only allowlisted phones can proceed to user lookup
 * - Only existing ACTIVE users are authenticated
 * - Never creates or restores users
 */
@Service
@Profile("test")
public class TestLoginService {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final Set<String> allowedPhones;

    public TestLoginService(UserService userService,
                            JwtTokenService jwtTokenService,
                            @Value("${petcare.test-login.allowed-phones:}") Set<String> allowedPhones) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
        this.allowedPhones = allowedPhones;
    }

    /**
     * Authenticates a test user by phone number.
     * Only allowlisted ACTIVE users can login.
     *
     * @param phone the user's phone number (must be in allowlist)
     * @return login response with token and user info
     * @throws BusinessException if phone not in allowlist or user not found/disabled
     */
    public TestLoginResponse authenticate(String phone) {
        if (!allowedPhones.contains(phone)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在或已禁用");
        }

        User user = userService.lambdaQuery()
                .eq(User::getPhone, phone)
                .eq(User::getStatus, "ACTIVE")
                .one();

        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在或已禁用");
        }

        String accessToken = jwtTokenService.signUserToken(user.getId());

        return new TestLoginResponse(
                "Bearer",
                accessToken,
                jwtTokenService.getExpirationSeconds(),
                new TestLoginResponse.UserInfo(
                        String.valueOf(user.getId()),
                        user.getNickname()
                )
        );
    }
}
