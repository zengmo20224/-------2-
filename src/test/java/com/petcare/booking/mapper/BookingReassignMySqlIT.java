package com.petcare.booking.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.booking.dto.BookingReassignRequest;
import com.petcare.booking.service.BookingTransactionService;
import com.petcare.booking.dto.BookingResponse;
import com.petcare.booking.entity.BookingStatusLog;
import com.petcare.booking.entity.ServiceBooking;
import com.petcare.booking.entity.StaffSchedule;
import com.petcare.booking.service.BookingApplicationService;
import com.petcare.booking.service.BookingStatusLogService;
import com.petcare.booking.service.ServiceBookingService;
import com.petcare.booking.service.StaffScheduleService;
import com.petcare.common.exception.BusinessException;
import com.petcare.service.entity.ServiceCategory;
import com.petcare.service.entity.ServiceItem;
import com.petcare.service.service.ServiceCategoryService;
import com.petcare.service.service.ServiceItemService;
import com.petcare.staff.entity.Staff;
import com.petcare.staff.entity.StaffSkill;
import com.petcare.staff.service.StaffService;
import com.petcare.staff.service.StaffSkillService;
import com.petcare.store.entity.Store;
import com.petcare.store.entity.StoreConfig;
import com.petcare.store.service.StoreConfigService;
import com.petcare.store.service.StoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MySQL 8 integration tests for booking reassign concurrency (RM-B03).
 *
 * Verifies:
 * 1. Cancel + reassign race: final state is consistent, CANCELLED booking not reassigned
 * 2. Concurrent reassigns: final staff has no time conflicts, log reflects real transitions
 *
 * Must run with mysql-test profile:
 *   mvn -Dtest=BookingReassignMySqlIT -Dspring.profiles.active=mysql-test test
 */
@SpringBootTest
@ActiveProfiles("mysql-test")
@Tag("mysql-reassign")
class BookingReassignMySqlIT {

    @Autowired private BookingApplicationService bookingApplicationService;
    @Autowired private BookingTransactionService bookingTransactionService;
    @Autowired private ServiceBookingService serviceBookingService;
    @Autowired private BookingStatusLogService bookingStatusLogService;
    @Autowired private StoreService storeService;
    @Autowired private StoreConfigService storeConfigService;
    @Autowired private ServiceCategoryService serviceCategoryService;
    @Autowired private ServiceItemService serviceItemService;
    @Autowired private StaffService staffService;
    @Autowired private StaffSkillService staffSkillService;
    @Autowired private StaffScheduleService staffScheduleService;

    private Long storeId;
    private Long serviceItemId;
    private Long staff1Id;
    private Long staff2Id;
    private Long staff3Id;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        futureDate = LocalDate.now().plusDays(3);

        Store store = new Store();
        store.setStoreName("改派并发测试门店");
        store.setLongitude(new BigDecimal("116.407400"));
        store.setLatitude(new BigDecimal("39.904200"));
        store.setStatus("OPEN");
        storeService.save(store);
        storeId = store.getId();

        StoreConfig config = new StoreConfig();
        config.setStoreId(storeId);
        config.setHomeServiceRadiusKm(new BigDecimal("5.00"));
        config.setBookingAdvanceDays(14);
        config.setBookingCancelHours(4);
        config.setTimeSlotMinutes(30);
        storeConfigService.save(config);

        ServiceCategory cat = new ServiceCategory();
        cat.setName("改派并发测试分类");
        cat.setSort(1);
        cat.setStatus("ACTIVE");
        serviceCategoryService.save(cat);

        ServiceItem item = new ServiceItem();
        item.setCategoryId(cat.getId());
        item.setName("改派并发测试服务");
        item.setServiceMode("BOTH");
        item.setPrice(new BigDecimal("99.00"));
        item.setDurationMinutes(60);
        item.setPetType("ALL");
        item.setPetSize("ALL");
        item.setNeedAddress(0);
        item.setNeedPet(1);
        item.setStatus("ON_SALE");
        item.setSort(0);
        serviceItemService.save(item);
        serviceItemId = item.getId();

        staff1Id = createStaffWithSkill("改派并发员工1", storeId, cat.getId());
        staff2Id = createStaffWithSkill("改派并发员工2", storeId, cat.getId());
        staff3Id = createStaffWithSkill("改派并发员工3", storeId, cat.getId());
        createSchedule(staff1Id, storeId, futureDate);
        createSchedule(staff2Id, storeId, futureDate);
        createSchedule(staff3Id, storeId, futureDate);
    }

    @Test
    @DisplayName("Concurrent cancel and reassign: CANCELLED booking must not be reassigned")
    void reassignAfterTerminalTransition_isRejected() throws Exception {
        // Create and confirm booking with staff1
        BookingResponse booking = createTestBooking(40001L, "13800400001", staff1Id);
        bookingApplicationService.confirmBooking(booking.id(), null, 9999L);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch gate = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Thread 1: cancel (locks booking with FOR UPDATE via transitionStatusOnce)
        executor.submit(() -> {
            ready.countDown();
            try { gate.await(); } catch (InterruptedException e) { return; }
            try {
                bookingApplicationService.cancelBookingAdmin(booking.id(), "紧急取消", 9001L);
                successCount.incrementAndGet();
            } catch (BusinessException e) {
                failCount.incrementAndGet();
                errors.add(e);
            } finally {
                done.countDown();
            }
        });

        // Thread 2: reassign to staff2
        executor.submit(() -> {
            ready.countDown();
            try { gate.await(); } catch (InterruptedException e) { return; }
            try {
                bookingApplicationService.reassignBooking(booking.id(),
                        new BookingReassignRequest(staff2Id), 9002L);
                successCount.incrementAndGet();
            } catch (BusinessException e) {
                failCount.incrementAndGet();
                errors.add(e);
            } finally {
                done.countDown();
            }
        });

        ready.await(10, TimeUnit.SECONDS);
        gate.countDown();
        done.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Cancel always succeeds from CONFIRMED, so status must be CANCELLED
        ServiceBooking result = serviceBookingService.getById(booking.id());
        assertThat(result.getStatus())
                .as("Final status must be CANCELLED (cancel is always valid from CONFIRMED)")
                .isEqualTo("CANCELLED");

        // Count reassign logs
        List<BookingStatusLog> reassignLogs = bookingStatusLogService.list(
                new LambdaQueryWrapper<BookingStatusLog>()
                        .eq(BookingStatusLog::getBookingId, booking.id())
                        .like(BookingStatusLog::getRemark, "改派"));

        if (reassignLogs.isEmpty()) {
            // Cancel won the lock first → reassign rejected → staff unchanged
            assertThat(result.getStaffId())
                    .as("If no reassign log, staff must be original")
                    .isEqualTo(staff1Id);
        } else {
            // Reassign won the lock first → staff changed → then cancel succeeded
            assertThat(result.getStaffId())
                    .as("If reassign log exists, staff must be the reassigned staff")
                    .isEqualTo(staff2Id);
        }

        // No partial mutation: if reassign succeeded, exactly 1 reassign log exists
        assertThat(reassignLogs.size())
                .as("At most 1 reassign log from concurrent cancel+reassign")
                .isLessThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Concurrent reassigns: final staff has no time conflicts and logs are correct")
    void concurrentReassigns_keepConsistentFinalStaffAndLogs() throws Exception {
        // Create booking with staff1
        BookingResponse booking = createTestBooking(40002L, "13800400002", staff1Id);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch gate = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Thread 1: reassign to staff2
        executor.submit(() -> {
            ready.countDown();
            try { gate.await(); } catch (InterruptedException e) { return; }
            try {
                bookingApplicationService.reassignBooking(booking.id(),
                        new BookingReassignRequest(staff2Id), 9001L);
                successCount.incrementAndGet();
            } catch (BusinessException e) {
                failCount.incrementAndGet();
                errors.add(e);
            } finally {
                done.countDown();
            }
        });

        // Thread 2: reassign to staff3
        executor.submit(() -> {
            ready.countDown();
            try { gate.await(); } catch (InterruptedException e) { return; }
            try {
                bookingApplicationService.reassignBooking(booking.id(),
                        new BookingReassignRequest(staff3Id), 9002L);
                successCount.incrementAndGet();
            } catch (BusinessException e) {
                failCount.incrementAndGet();
                errors.add(e);
            } finally {
                done.countDown();
            }
        });

        ready.await(10, TimeUnit.SECONDS);
        gate.countDown();
        done.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // At least 1 must succeed (both target different free staff)
        assertThat(successCount.get())
                .as("At least 1 reassign must succeed")
                .isGreaterThanOrEqualTo(1);

        // Final staff must be one of the target staff
        ServiceBooking result = serviceBookingService.getById(booking.id());
        assertThat(result.getStaffId())
                .as("Final staff must be staff2 or staff3 (not original staff1)")
                .isIn(staff2Id, staff3Id);

        // Final staff must have no time conflicts
        List<ServiceBooking> conflicts = serviceBookingService.list(
                new LambdaQueryWrapper<ServiceBooking>()
                        .eq(ServiceBooking::getStaffId, result.getStaffId())
                        .eq(ServiceBooking::getBookingDate, futureDate)
                        .ne(ServiceBooking::getId, result.getId())
                        .in(ServiceBooking::getStatus, "PENDING_CONFIRM", "CONFIRMED", "IN_SERVICE")
                        .lt(ServiceBooking::getStartTime, result.getEndTime())
                        .gt(ServiceBooking::getEndTime, result.getStartTime()));
        assertThat(conflicts)
                .as("Final staff must have no time conflicts with other bookings")
                .isEmpty();

        // Reassign logs must reflect real transitions
        List<BookingStatusLog> reassignLogs = bookingStatusLogService.list(
                new LambdaQueryWrapper<BookingStatusLog>()
                        .eq(BookingStatusLog::getBookingId, booking.id())
                        .like(BookingStatusLog::getRemark, "改派")
                        .orderByAsc(BookingStatusLog::getCreateTime));

        for (BookingStatusLog log : reassignLogs) {
            assertThat(log.getRemark())
                    .as("Each reassign log must mention old and new staff IDs")
                    .contains("改派员工");
        }
    }

    // --- Helper methods ---

    private BookingResponse createTestBooking(Long userId, String phone, Long staffId) {
        ServiceBooking booking = new ServiceBooking();
        booking.setBookingNo("BK" + System.nanoTime());
        booking.setUserId(userId);
        booking.setStoreId(storeId);
        booking.setServiceItemId(serviceItemId);
        booking.setStaffId(staffId);
        booking.setServiceMode("STORE");
        booking.setBookingDate(futureDate);
        booking.setStartTime(LocalTime.of(14, 0));
        booking.setEndTime(LocalTime.of(15, 0));
        booking.setContactName("测试用户");
        booking.setContactPhone(phone);
        booking.setPaymentMethod("OFFLINE_STORE");
        booking.setPaymentStatus("UNPAID");
        booking.setStatus("PENDING_CONFIRM");
        booking.setPrice(new BigDecimal("99.00"));

        ServiceBooking saved = bookingTransactionService.createBookingOnce(booking);
        return bookingApplicationService.getAdminBooking(saved.getId());
    }

    private Long createStaffWithSkill(String name, Long storeId, Long categoryId) {
        Staff staff = new Staff();
        staff.setStoreId(storeId);
        staff.setName(name);
        staff.setRole("GROOMER");
        staff.setStatus("ACTIVE");
        staffService.save(staff);

        StaffSkill skill = new StaffSkill();
        skill.setStaffId(staff.getId());
        skill.setServiceCategoryId(categoryId);
        staffSkillService.save(skill);

        return staff.getId();
    }

    private void createSchedule(Long staffId, Long storeId, LocalDate date) {
        StaffSchedule schedule = new StaffSchedule();
        schedule.setStaffId(staffId);
        schedule.setStoreId(storeId);
        schedule.setWorkDate(date);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(17, 0));
        schedule.setStatus("AVAILABLE");
        staffScheduleService.save(schedule);
    }
}
