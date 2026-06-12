package com.petcare.user.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.common.security.SecurityContextHelper;
import com.petcare.user.dto.UpdateUserProfileRequest;
import com.petcare.user.dto.UserProfileResponse;
import com.petcare.user.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for current user profile operations.
 * All endpoints require ROLE_USER explicitly — ADMIN tokens get 403.
 * Current user ID is always derived from SecurityContext, never from request parameters.
 */
@RestController
@RequestMapping("/api/v1/user")
@PreAuthorize("hasRole('USER')")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getCurrentProfile() {
        Long currentUserId = requireCurrentUserId();
        UserProfileResponse profile = userProfileService.getCurrentProfile(currentUserId);
        return ApiResponse.ok(profile);
    }

    @PutMapping("/profile")
    public ApiResponse<UserProfileResponse> updateCurrentProfile(
            @Valid @RequestBody UpdateUserProfileRequest request) {
        Long currentUserId = requireCurrentUserId();
        UserProfileResponse updated = userProfileService.updateCurrentProfile(currentUserId, request);
        return ApiResponse.ok(updated);
    }

    private Long requireCurrentUserId() {
        return SecurityContextHelper.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录"));
    }
}
