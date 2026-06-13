package com.petcare.user.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.user.dto.AddressUpsertRequest;
import com.petcare.user.entity.User;
import com.petcare.user.entity.UserAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

/**
 * Spring integration tests proving real transaction rollback on address write failures.
 *
 * Uses @SpyBean UserAddressService to selectively make update() return false,
 * simulating a write failure. Then verifies the database state is unchanged —
 * the @Transactional service method rolls back all prior writes.
 *
 * Test methods do NOT use @Transactional so that the @Transactional on the
 * application service runs in its own transaction and actually rolls back on failure.
 * @AfterEach manually cleans up data to avoid H2 cross-test accumulation.
 *
 * @SpyBean stubs are automatically reset by Spring Boot after each test method,
 * so other tests in the same context are not affected.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AddressTransactionRollbackTest {

    @Autowired
    private AddressApplicationService addressApplicationService;

    @SpyBean
    private UserAddressService addressService;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("create with unset failure → rollback, old default address still default")
    void createUnsetFailure_oldDefaultStillDefault() {
        // Arrange: active user with one default address
        User user = createUser("13800999001", "回滚用户A");
        UserAddress existingDefault = createDefaultAddress(user.getId(), "原默认地址");

        // Capture state before
        Integer beforeIsDefault = addressService.getById(existingDefault.getId()).getIsDefault();
        assertThat(beforeIsDefault).isEqualTo(1);

        // Make the update (unsetOtherDefaults) return false to force rollback
        // update is called with a LambdaUpdateWrapper (subclass of Wrapper)
        doReturn(false).when(addressService).update(any(Wrapper.class));

        AddressUpsertRequest request = new AddressUpsertRequest(
                "新默认", "13800999002", "广东省", "深圳市",
                "南山区", "科技园2号",
                new BigDecimal("113.934528"), new BigDecimal("22.540503"), true);

        // Act: create should throw IllegalStateException (transaction rolled back)
        assertThatThrownBy(() -> addressApplicationService.createCurrentUserAddress(user.getId(), request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("取消旧默认地址失败");

        // Assert: old default address is still default (rollback worked)
        UserAddress after = addressService.getById(existingDefault.getId());
        assertThat(after).as("address must still exist after rollback").isNotNull();
        assertThat(after.getIsDefault())
                .as("old default address must still be default after rollback")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("delete with promote failure → rollback, address still exists")
    void deletePromoteFailure_addressStillExists() {
        // Arrange: active user with one default address and one non-default
        User user = createUser("13800999002", "回滚用户B");
        UserAddress defaultAddr = createDefaultAddress(user.getId(), "默认地址");
        UserAddress otherAddr = createNonDefaultAddress(user.getId(), "其他地址");

        // Make the update (promoteNextDefault) return false to force rollback
        doReturn(false).when(addressService).update(any(Wrapper.class));

        // Act: delete should throw IllegalStateException (transaction rolled back)
        assertThatThrownBy(() -> addressApplicationService.deleteCurrentUserAddress(user.getId(), defaultAddr.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("默认地址提升失败");

        // Assert: deleted address still exists (rollback restored it)
        UserAddress after = addressService.getById(defaultAddr.getId());
        assertThat(after).as("deleted address must still exist after rollback").isNotNull();
        assertThat(after.getIsDefault())
                .as("deleted default address must still be default after rollback")
                .isEqualTo(1);
    }

    // ======================== Helpers ========================

    private User createUser(String phone, String nickname) {
        User user = new User();
        user.setPhone(phone);
        user.setNickname(nickname);
        user.setStatus("ACTIVE");
        user.setOpenid("test_rb_openid_" + phone);
        user.setUnionid("test_rb_unionid_" + phone);
        userService.save(user);
        return user;
    }

    private UserAddress createDefaultAddress(Long userId, String contactName) {
        UserAddress addr = new UserAddress();
        addr.setUserId(userId);
        addr.setContactName(contactName);
        addr.setContactPhone("13800999001");
        addr.setProvince("广东省");
        addr.setCity("深圳市");
        addr.setDetailAddress("科技园1号");
        addr.setIsDefault(1);
        addressService.save(addr);
        return addr;
    }

    private UserAddress createNonDefaultAddress(Long userId, String contactName) {
        UserAddress addr = new UserAddress();
        addr.setUserId(userId);
        addr.setContactName(contactName);
        addr.setContactPhone("13800999003");
        addr.setProvince("广东省");
        addr.setCity("深圳市");
        addr.setDetailAddress("科技园2号");
        addr.setIsDefault(0);
        addressService.save(addr);
        return addr;
    }

    @AfterEach
    void cleanup() {
        // Clean up all addresses and users created during tests
        List<UserAddress> testAddresses = addressService.list(
                new LambdaQueryWrapper<UserAddress>()
                        .likeRight(UserAddress::getContactPhone, "13800999"));
        if (!testAddresses.isEmpty()) {
            addressService.removeByIds(testAddresses.stream().map(UserAddress::getId).toList());
        }

        List<User> testUsers = userService.list(
                new LambdaQueryWrapper<User>()
                        .likeRight(User::getPhone, "13800999"));
        if (!testUsers.isEmpty()) {
            userService.removeByIds(testUsers.stream().map(User::getId).toList());
        }
    }
}
