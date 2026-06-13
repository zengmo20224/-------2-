package com.petcare.user.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.persistence.AbstractTcMySqlIT;
import com.petcare.user.dto.AddressUpsertRequest;
import com.petcare.user.entity.User;
import com.petcare.user.entity.UserAddress;
import com.petcare.user.service.AddressApplicationService;
import com.petcare.user.service.UserAddressService;
import com.petcare.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MySQL 8 integration tests for address default address concurrency.
 * Verifies that user row locking (SELECT FOR UPDATE) serializes
 * concurrent default address changes so that exactly one default exists.
 *
 * Run with the Testcontainers MySQL gate:
 *   mvn clean test -Ptc-mysql
 */
@Tag("tc-mysql")
class AddressDefaultConcurrencyMySqlIT extends AbstractTcMySqlIT {

    @Autowired
    private AddressApplicationService addressApplicationService;

    @Autowired
    private UserAddressService addressService;

    @Autowired
    private UserService userService;

    private Long userId;
    private static final AtomicLong userSeq = new AtomicLong(System.nanoTime());
    private Long testUserId;

    @BeforeEach
    void setUp() {
        long seq = userSeq.incrementAndGet();
        User user = new User();
        user.setPhone("13800888" + String.format("%03d", Math.abs(seq % 1000)));
        user.setNickname("并发测试用户" + seq);
        user.setStatus("ACTIVE");
        user.setOpenid("tc_conc_openid_" + seq);
        user.setUnionid("tc_conc_unionid_" + seq);
        userService.save(user);
        userId = user.getId();
        testUserId = userId;
    }

    @AfterEach
    void tearDown() {
        // Clean up addresses and user created by this test
        List<UserAddress> addresses = addressService.list(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, testUserId));
        if (!addresses.isEmpty()) {
            addressService.removeByIds(addresses.stream().map(UserAddress::getId).toList());
        }
        userService.removeById(testUserId);
    }

    @Test
    @DisplayName("concurrent update of two addresses to default → exactly 1 default")
    void concurrentUpdateDefault_exactlyOneDefault() throws Exception {
        // Arrange: create two non-default addresses
        UserAddress addr1 = createNonDefaultAddress(userId, "地址A", "13800888001");
        UserAddress addr2 = createNonDefaultAddress(userId, "地址B", "13800888002");

        // Also set one as default initially to test unset
        addr1.setIsDefault(1);
        addressService.updateById(addr1);

        AtomicInteger successCount = new AtomicInteger(0);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch gate = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Task 1: update addr2 to default
        Runnable task1 = () -> {
            ready.countDown();
            try { gate.await(); } catch (InterruptedException e) { return; }
            try {
                AddressUpsertRequest req = new AddressUpsertRequest(
                        "地址A", "13800888001", "广东省", "深圳市",
                        "南山区", "科技园1号",
                        new BigDecimal("113.934528"), new BigDecimal("22.540503"), true);
                addressApplicationService.updateCurrentUserAddress(userId, addr1.getId(), req);
                successCount.incrementAndGet();
            } catch (BusinessException e) {
                errors.add(e);
            } finally {
                done.countDown();
            }
        };

        // Task 2: update addr2 to default
        Runnable task2 = () -> {
            ready.countDown();
            try { gate.await(); } catch (InterruptedException e) { return; }
            try {
                AddressUpsertRequest req = new AddressUpsertRequest(
                        "地址B", "13800888002", "广东省", "深圳市",
                        "南山区", "科技园2号",
                        new BigDecimal("113.934528"), new BigDecimal("22.540503"), true);
                addressApplicationService.updateCurrentUserAddress(userId, addr2.getId(), req);
                successCount.incrementAndGet();
            } catch (BusinessException e) {
                errors.add(e);
            } finally {
                done.countDown();
            }
        };

        executor.submit(task1);
        executor.submit(task2);

        ready.await(10, TimeUnit.SECONDS);
        gate.countDown();

        done.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Both should succeed (serialized by user row lock)
        assertThat(errors).as("No unexpected errors").isEmpty();
        assertThat(successCount.get()).isEqualTo(2);

        // Exactly 1 default address
        long defaultCount = countDefaults(userId);
        assertThat(defaultCount)
                .as("Exactly 1 default address after concurrent updates")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("concurrent create first address with isDefault → exactly 1 default")
    void concurrentCreateFirstDefault_exactlyOneDefault() throws Exception {
        AtomicInteger successCount = new AtomicInteger(0);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch gate = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable task1 = () -> {
            ready.countDown();
            try { gate.await(); } catch (InterruptedException e) { return; }
            try {
                AddressUpsertRequest req = new AddressUpsertRequest(
                        "并发地址A", "13800888003", "广东省", "深圳市",
                        "南山区", "科技园1号",
                        new BigDecimal("113.934528"), new BigDecimal("22.540503"), true);
                addressApplicationService.createCurrentUserAddress(userId, req);
                successCount.incrementAndGet();
            } catch (BusinessException e) {
                errors.add(e);
            } finally {
                done.countDown();
            }
        };

        Runnable task2 = () -> {
            ready.countDown();
            try { gate.await(); } catch (InterruptedException e) { return; }
            try {
                AddressUpsertRequest req = new AddressUpsertRequest(
                        "并发地址B", "13800888004", "广东省", "深圳市",
                        "南山区", "科技园2号",
                        new BigDecimal("113.934528"), new BigDecimal("22.540503"), true);
                addressApplicationService.createCurrentUserAddress(userId, req);
                successCount.incrementAndGet();
            } catch (BusinessException e) {
                errors.add(e);
            } finally {
                done.countDown();
            }
        };

        executor.submit(task1);
        executor.submit(task2);

        ready.await(10, TimeUnit.SECONDS);
        gate.countDown();

        done.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Both should succeed (serialized by user row lock)
        assertThat(errors).as("No unexpected errors").isEmpty();
        assertThat(successCount.get()).isEqualTo(2);

        // Exactly 1 default address
        long defaultCount = countDefaults(userId);
        assertThat(defaultCount)
                .as("Exactly 1 default address after concurrent creates")
                .isEqualTo(1);
    }

    // ======================== Helpers ========================

    private UserAddress createNonDefaultAddress(Long userId, String name, String phone) {
        UserAddress addr = new UserAddress();
        addr.setUserId(userId);
        addr.setContactName(name);
        addr.setContactPhone(phone);
        addr.setProvince("广东省");
        addr.setCity("深圳市");
        addr.setDetailAddress("科技园1号");
        addr.setIsDefault(0);
        addressService.save(addr);
        return addr;
    }

    private long countDefaults(Long userId) {
        return addressService.count(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1));
    }
}
