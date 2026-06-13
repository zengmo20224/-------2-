package com.petcare.user.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.user.dto.AddressUpsertRequest;
import com.petcare.user.entity.User;
import com.petcare.user.entity.UserAddress;
import com.petcare.user.service.impl.AddressApplicationServiceImpl;
import com.petcare.user.service.impl.UserServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure Mockito unit tests for AddressApplicationServiceImpl.
 * Covers failure paths and invariant enforcement that are difficult
 * to trigger through integration tests with a real database.
 */
@ExtendWith(MockitoExtension.class)
class AddressApplicationServiceImplTest {

    @BeforeAll
    static void initMybatisPlusLambdaCache() {
        // LambdaUpdateWrapper requires MP's TableInfoHelper lambda cache.
        // In a pure Mockito test there is no Spring context, so we init it manually.
        if (TableInfoHelper.getTableInfo(UserAddress.class) == null) {
            MybatisConfiguration configuration = new MybatisConfiguration();
            MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
            assistant.setCurrentNamespace("com.petcare.user.mapper.UserAddressMapper");
            TableInfoHelper.initTableInfo(assistant, UserAddress.class);
        }
    }

    private final UserAddressService addressService = mock(UserAddressService.class);
    private final UserServiceImpl userService = mock(UserServiceImpl.class);
    private final AddressApplicationServiceImpl service =
            new AddressApplicationServiceImpl(addressService, userService);

    // ======================== Create: unset failure ========================

    @Nested
    @DisplayName("create — unsetOtherDefaults returns false")
    class CreateUnsetFailure {

        @Test
        @DisplayName("unset returns false → IllegalStateException, save not called")
        void unsetReturnsFalse_saveNotCalled() {
            User user = new User();
            user.setId(1L);
            user.setStatus("ACTIVE");
            when(userService.lockActiveUser(1L)).thenReturn(user);
            // count > 0 to enter the unset branch
            when(addressService.count(any())).thenReturn(1L);
            // update (unsetOtherDefaults) returns false
            when(addressService.update(any(LambdaUpdateWrapper.class))).thenReturn(false);

            AddressUpsertRequest request = createRequest(true);

            assertThatThrownBy(() -> service.createCurrentUserAddress(1L, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("取消旧默认地址失败");

            verify(addressService).update(any(LambdaUpdateWrapper.class));
            verify(addressService, never()).save(any(UserAddress.class));
        }
    }

    // ======================== Create: save failure ========================

    @Nested
    @DisplayName("create — save returns false")
    class CreateSaveFailure {

        @Test
        @DisplayName("save returns false → IllegalStateException")
        void saveReturnsFalse_throwsException() {
            User user = new User();
            user.setId(1L);
            user.setStatus("ACTIVE");
            when(userService.lockActiveUser(1L)).thenReturn(user);
            // First address: no unset needed
            when(addressService.count(any())).thenReturn(0L);
            when(addressService.save(any(UserAddress.class))).thenReturn(false);

            AddressUpsertRequest request = createRequest(false);

            assertThatThrownBy(() -> service.createCurrentUserAddress(1L, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("地址保存失败");

            verify(addressService).save(any(UserAddress.class));
        }
    }

    // ======================== Update: unset failure ========================

    @Nested
    @DisplayName("update — unsetOtherDefaults returns false")
    class UpdateUnsetFailure {

        @Test
        @DisplayName("unset returns false → IllegalStateException, target update not called")
        void unsetReturnsFalse_targetUpdateNotCalled() {
            User user = new User();
            user.setId(1L);
            user.setStatus("ACTIVE");
            when(userService.lockActiveUser(1L)).thenReturn(user);

            // Existing address is NOT default
            UserAddress existing = new UserAddress();
            existing.setId(10L);
            existing.setUserId(1L);
            existing.setIsDefault(0);
            when(addressService.getOne(any())).thenReturn(existing);

            // update (unsetOtherDefaults) returns false
            when(addressService.update(any(LambdaUpdateWrapper.class))).thenReturn(false);

            AddressUpsertRequest request = createRequest(true);

            assertThatThrownBy(() -> service.updateCurrentUserAddress(1L, 10L, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("取消旧默认地址失败");

            // The second update (target) should not happen because exception was thrown
            // We verify update was called only once (for unset), not twice
            verify(addressService).update(any(LambdaUpdateWrapper.class));
        }
    }

    // ======================== Update: target returns false ========================

    @Nested
    @DisplayName("update — target update returns false")
    class UpdateTargetFailure {

        @Test
        @DisplayName("target update returns false → 404 RESOURCE_NOT_FOUND")
        void targetUpdateReturnsFalse_throws404() {
            User user = new User();
            user.setId(1L);
            user.setStatus("ACTIVE");
            when(userService.lockActiveUser(1L)).thenReturn(user);

            UserAddress existing = new UserAddress();
            existing.setId(10L);
            existing.setUserId(1L);
            existing.setIsDefault(1);
            when(addressService.getOne(any())).thenReturn(existing);

            // update returns false for target
            when(addressService.update(any(LambdaUpdateWrapper.class))).thenReturn(false);

            AddressUpsertRequest request = createRequest(true);

            assertThatThrownBy(() -> service.updateCurrentUserAddress(1L, 10L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }

    // ======================== Delete: promote failure ========================

    @Nested
    @DisplayName("delete — promoteNextDefault returns false")
    class DeletePromoteFailure {

        @Test
        @DisplayName("promote update returns false → IllegalStateException")
        void promoteReturnsFalse_throwsException() {
            User user = new User();
            user.setId(1L);
            user.setStatus("ACTIVE");
            when(userService.lockActiveUser(1L)).thenReturn(user);

            // Existing default address
            UserAddress existing = new UserAddress();
            existing.setId(10L);
            existing.setUserId(1L);
            existing.setIsDefault(1);
            when(addressService.getOne(any())).thenReturn(existing);

            // Remove succeeds
            when(addressService.remove(any())).thenReturn(true);

            // Remaining addresses exist but update fails
            UserAddress remaining = new UserAddress();
            remaining.setId(11L);
            remaining.setUserId(1L);
            when(addressService.list(any(LambdaQueryWrapper.class))).thenReturn(java.util.List.of(remaining));
            when(addressService.update(any(LambdaUpdateWrapper.class))).thenReturn(false);

            assertThatThrownBy(() -> service.deleteCurrentUserAddress(1L, 10L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("默认地址提升失败");
        }
    }

    // ======================== lockActiveUser: disabled user ========================

    @Nested
    @DisplayName("lockActiveUser — disabled/nonexistent user")
    class LockActiveUserDisabled {

        @Test
        @DisplayName("disabled user throws UNAUTHORIZED, no write operations")
        void disabledUser_throwsUnauthorized() {
            when(userService.lockActiveUser(1L))
                    .thenThrow(new BusinessException(ErrorCode.UNAUTHORIZED, "认证失败,请先登录"));

            AddressUpsertRequest request = createRequest(false);

            assertThatThrownBy(() -> service.createCurrentUserAddress(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.UNAUTHORIZED);

            verify(addressService, never()).save(any());
            verify(addressService, never()).update(any(LambdaUpdateWrapper.class));
        }
    }

    // ======================== Helpers ========================

    private static AddressUpsertRequest createRequest(boolean isDefault) {
        return new AddressUpsertRequest(
                "张三", "13800138000", "广东省", "深圳市",
                "南山区", "科技园1号",
                new BigDecimal("113.934528"), new BigDecimal("22.540503"),
                isDefault);
    }
}
