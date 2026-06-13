package com.petcare.user.service;

import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.user.dto.UpdateUserProfileRequest;
import com.petcare.user.dto.UserProfileResponse;
import com.petcare.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Service-layer integration tests for UserProfileServiceImpl.
 * Directly calls service methods (not through MockMvc/JWT filter)
 * to verify ACTIVE status enforcement at the service boundary.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserProfileServiceImplTest {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserService userService;

    // ======================== Read ========================

    @Nested
    @DisplayName("getCurrentProfile — ACTIVE status enforcement")
    class GetCurrentProfile {

        @Test
        @DisplayName("ACTIVE user can read profile via service")
        void activeUserCanReadProfile() {
            User user = createUser("13800138001", "测试用户", "ACTIVE");

            UserProfileResponse response = userProfileService.getCurrentProfile(user.getId());

            assertThat(response).isNotNull();
            assertThat(response.nickname()).isEqualTo("测试用户");
            assertThat(response.phone()).isEqualTo("138****8001");
        }

        @Test
        @DisplayName("DISABLED user read throws unauthorized")
        void disabledUserReadThrowsUnauthorized() {
            User user = createUser("13800138002", "禁用用户", "DISABLED");

            assertThatThrownBy(() -> userProfileService.getCurrentProfile(user.getId()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.UNAUTHORIZED);
        }

        @Test
        @DisplayName("non-existent user read throws unauthorized")
        void nonExistentUserReadThrowsUnauthorized() {
            assertThatThrownBy(() -> userProfileService.getCurrentProfile(999999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.UNAUTHORIZED);
        }
    }

    // ======================== Update ========================

    @Nested
    @DisplayName("updateCurrentProfile — ACTIVE status enforcement")
    class UpdateCurrentProfile {

        @Test
        @DisplayName("ACTIVE user can update profile via service")
        void activeUserCanUpdateProfile() {
            User user = createUser("13800138003", "旧昵称", "ACTIVE");

            UpdateUserProfileRequest request = new UpdateUserProfileRequest("新昵称", "https://example.com/a.png");
            UserProfileResponse response = userProfileService.updateCurrentProfile(user.getId(), request);

            assertThat(response).isNotNull();
            assertThat(response.nickname()).isEqualTo("新昵称");
        }

        @Test
        @DisplayName("DISABLED user update throws unauthorized, database unchanged")
        void disabledUserUpdateThrowsUnauthorized() {
            User user = createUser("13800138004", "禁用更新", "DISABLED");
            String originalNickname = user.getNickname();

            UpdateUserProfileRequest request = new UpdateUserProfileRequest("新昵称", null);
            assertThatThrownBy(() -> userProfileService.updateCurrentProfile(user.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.UNAUTHORIZED);

            // Verify database was NOT modified
            User unchanged = userService.getById(user.getId());
            assertThat(unchanged.getNickname()).isEqualTo(originalNickname);
        }

        @Test
        @DisplayName("user disabled after first read causes update failure, data unchanged")
        void userDisabledAfterReadCausesUpdateFailure() {
            User user = createUser("13800138005", "竞态测试", "ACTIVE");

            // First read succeeds
            UserProfileResponse profile = userProfileService.getCurrentProfile(user.getId());
            assertThat(profile).isNotNull();
            assertThat(profile.nickname()).isEqualTo("竞态测试");

            // Simulate race condition: user becomes DISABLED between read and update
            user.setStatus("DISABLED");
            userService.updateById(user);

            // Update should fail — throws UNAUTHORIZED
            UpdateUserProfileRequest request = new UpdateUserProfileRequest("新昵称", null);
            assertThatThrownBy(() -> userProfileService.updateCurrentProfile(user.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.UNAUTHORIZED);

            // Verify nickname was NOT changed
            User unchanged = userService.getById(user.getId());
            assertThat(unchanged.getNickname()).isEqualTo("竞态测试");
        }

        @Test
        @DisplayName("update only modifies nickname and avatarUrl, not phone/openid/status")
        void updateOnlyModifiesAllowedFields() {
            User user = createUser("13800138006", "字段保护", "ACTIVE");
            String originalPhone = user.getPhone();
            String originalOpenid = user.getOpenid();
            String originalStatus = user.getStatus();

            UpdateUserProfileRequest request = new UpdateUserProfileRequest("更新昵称", "https://cdn.example.com/avatar.png");
            userProfileService.updateCurrentProfile(user.getId(), request);

            User updated = userService.getById(user.getId());
            assertThat(updated.getNickname()).isEqualTo("更新昵称");
            assertThat(updated.getAvatarUrl()).isEqualTo("https://cdn.example.com/avatar.png");
            // Protected fields must NOT change
            assertThat(updated.getPhone()).isEqualTo(originalPhone);
            assertThat(updated.getOpenid()).isEqualTo(originalOpenid);
            assertThat(updated.getStatus()).isEqualTo(originalStatus);
        }
    }

    // ======================== Helpers ========================

    private User createUser(String phone, String nickname, String status) {
        User user = new User();
        user.setPhone(phone);
        user.setNickname(nickname);
        user.setStatus(status);
        user.setOpenid("test_openid_" + phone);
        user.setUnionid("test_unionid_" + phone);
        userService.save(user);
        return user;
    }
}
