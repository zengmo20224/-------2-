package com.petcare.user.service;

import com.petcare.user.dto.UpdateUserProfileRequest;
import com.petcare.user.dto.UserProfileResponse;

/**
 * Application service for current user profile operations.
 * Only handles the currently authenticated user's own profile.
 */
public interface UserProfileService {

    /**
     * Gets the profile of the currently authenticated user.
     *
     * @param currentUserId the user ID from the security context
     * @return the user's profile response
     */
    UserProfileResponse getCurrentProfile(Long currentUserId);

    /**
     * Updates the profile of the currently authenticated user.
     * Only nickname and avatarUrl can be modified.
     *
     * @param currentUserId the user ID from the security context
     * @param request       the update request containing allowed fields
     * @return the updated user profile response
     */
    UserProfileResponse updateCurrentProfile(Long currentUserId, UpdateUserProfileRequest request);
}
