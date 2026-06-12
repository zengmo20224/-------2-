package com.petcare.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.user.dto.UpdateUserProfileRequest;
import com.petcare.user.dto.UserProfileResponse;
import com.petcare.user.entity.User;
import com.petcare.user.service.UserProfileService;
import com.petcare.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of UserProfileService.
 * Queries and updates only the current user's own profile through UserService.
 */
@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserService userService;

    public UserProfileServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserProfileResponse getCurrentProfile(Long currentUserId) {
        User user = getActiveUser(currentUserId);
        return UserProfileResponse.from(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateCurrentProfile(Long currentUserId, UpdateUserProfileRequest request) {
        User user = getActiveUser(currentUserId);

        // Trim nickname whitespace; reject empty after trim
        String trimmedNickname = request.nickname() != null ? request.nickname().trim() : null;
        if (trimmedNickname == null || trimmedNickname.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "昵称不能为空");
        }

        // Validate avatarUrl protocol when provided
        validateAvatarUrl(request.avatarUrl());

        // Only update allowed fields using explicit LambdaUpdateWrapper
        // This ensures null avatarUrl is written to DB (clearing the field)
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<User>()
                .eq(User::getId, user.getId())
                .set(User::getNickname, trimmedNickname)
                .set(User::getAvatarUrl, request.avatarUrl());
        if (!userService.update(updateWrapper)) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "资料更新失败，请重试");
        }

        // Return latest data
        User updated = getActiveUser(currentUserId);
        return UserProfileResponse.from(updated);
    }

    private void validateAvatarUrl(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return;
        }
        String lower = avatarUrl.toLowerCase();
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "头像URL只允许http或https协议");
        }
    }

    private User getActiveUser(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在或已禁用");
        }
        return user;
    }
}
