package com.petcare.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.booking.dto.BookingCreateRequest;
import com.petcare.booking.dto.BookingResponse;
import com.petcare.booking.entity.BookingStatusLog;
import com.petcare.booking.entity.ServiceBooking;
import com.petcare.booking.entity.StaffSchedule;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Reassign transaction regression tests (RM-B03).
 *
 * Verifies:
 * 1. Reassign to busy staff is rejected without mutating booking or log
 * 2. Reassign on cancelled booking is rejected without mutation (tests transaction-level status check)
 * 3. Reassign uses locked booking snapshot for conflict check, not stale parameters
 *
 * Runs on H2 to validate logic. MySQL concurrency tested separately.
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("reassign-transaction")
class BookingReassignTransactionTest {

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
    private Long categoryId;
    private Long staff1Id;
    private Long staff2Id;
    private Long staff3Id;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        futureDate = LocalDate.now().plusDays(3);

        Store store = new Store();
        store.setStoreName("改派测试门店");
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
        cat.setName("改派测试分类");
        cat.setSort(1);
        cat.setStatus("ACTIVE");
        serviceCategoryService.save(cat);
        categoryId = cat.getId();

        ServiceItem item = new ServiceItem();
        item.setCategoryId(categoryId);
        item.setName("改派测试服务");
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

        // Create 3 staff with matching skill and schedule
        staff1Id = createStaffWithSkill("改派测试员工1", storeId, categoryId);
        staff2Id = createStaffWithSkill("改派测试员工2", storeId, categoryId);
        staff3Id = createStaffWithSkill("改派测试员工3", storeId, categoryId);
        createSchedule(staff1Id, storeId, futureDate);
        createSchedule(staff2Id, storeId, futureDate);
        createSchedule(staff3Id, storeId, futureDate);
    }

    @Test
    @DisplayName("Reassign to busy staff: rejected without mutating booking or log")
    void reassignToBusyStaff_isRejectedWithoutMutation() {
        // Create booking with staff1 at 14:00-15:00
        BookingResponse booking = createTestBooking(80001L, "13800800001", staff1Id);

        // Create a conflicting booking with staff2 at 14:00-15:00
        createTestBooking(80002L, "13800800002", staff2Id);

        long logCountBefore = countLogsForBooking(booking.id());

        // Attempt reassign to staff2 (conflict)
        assertThatThrownBy(() ->
                bookingApplicationService.reassignBooking(booking.id(),
                        new com.petcare.booking.dto.BookingReassignRequest(staff2Id), 9999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("冲突");

        // Booking unchanged
        ServiceBooking unchanged = serviceBookingService.getById(booking.id());
        assertThat(unchanged.getStaffId()).isEqualTo(staff1Id);
        assertThat(unchanged.getStatus()).isEqualTo("PENDING_CONFIRM");

        // No extra log
        long logCountAfter = countLogsForBooking(booking.id());
        assertThat(logCountAfter)
                .as("Rejected reassign must NOT create extra log")
                .isEqualTo(logCountBefore);
    }

    @Test
    @DisplayName("Reassign on cancelled booking: rejected without mutation (transaction-level check)")
    void reassignOnCancelledBooking_isRejectedWithoutMutation() {
        // Create and cancel a booking
        BookingResponse booking = createTestBooking(80003L, "13800800003", staff1Id);
        bookingApplicationService.cancelBookingAdmin(booking.id(), "测试取消", 9999L);

        long logCountBefore = countLogsForBooking(booking.id());

        // Call transaction service directly (bypasses app service status check)
        // This tests that the transaction validates status AFTER locking
        assertThatThrownBy(() ->
                bookingTransactionService.reassignBookingOnce(
                        booking.id(), staff2Id,
                        futureDate, LocalTime.of(14, 0), LocalTime.of(15, 0), 9999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("预约");

        // Staff unchanged
        ServiceBooking unchanged = serviceBookingService.getById(booking.id());
        assertThat(unchanged.getStaffId()).isEqualTo(staff1Id);
        assertThat(unchanged.getStatus()).isEqualTo("CANCELLED");

        // No extra log
        long logCountAfter = countLogsForBooking(booking.id());
        assertThat(logCountAfter)
                .as("Rejected reassign on cancelled booking must NOT create extra log")
                .isEqualTo(logCountBefore);
    }

    @Test
    @DisplayName("Reassign uses locked booking snapshot for conflict check, not stale parameters")
    void reassignUsesLockedBookingSnapshot() {
        // Create booking with staff1 at 14:00-15:00
        BookingResponse booking = createTestBooking(80004L, "13800800004", staff1Id);

        // Create a conflicting booking with staff2 at 14:00-15:00 (same time as original booking)
        createTestBooking(80005L, "13800800005", staff2Id);

        long logCountBefore = countLogsForBooking(booking.id());

        // Call transaction service with STALE time parameters (16:00-17:00, which doesn't conflict)
        // Current code BUG: uses stale 16:00-17:00 for conflict check → no conflict → reassign succeeds
        // Fixed code: reads actual 14:00-15:00 from locked booking → conflict detected → rejected
        assertThatThrownBy(() ->
                bookingTransactionService.reassignBookingOnce(
                        booking.id(), staff2Id,
                        futureDate, LocalTime.of(16, 0), LocalTime.of(17, 0), 9999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("冲突");

        // Staff unchanged
        ServiceBooking unchanged = serviceBookingService.getById(booking.id());
        assertThat(unchanged.getStaffId())
                .as("Staff must not change when reassign uses correct locked snapshot")
                .isEqualTo(staff1Id);

        // No extra log
        long logCountAfter = countLogsForBooking(booking.id());
        assertThat(logCountAfter)
                .as("Rejected reassign must NOT create extra log")
                .isEqualTo(logCountBefore);
    }

    // --- Helper methods ---

    private BookingResponse createTestBooking(Long userId, String phone, Long staffId) {
        // To force a specific staff, we create only that staff's skill and schedule
        // But since we have 3 staff in setUp, auto-assignment might pick any of them.
        // Use direct transaction service to force the staff assignment.
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
        return new BookingResponse(saved.getId(), saved.getBookingNo(), saved.getUserId(),
                saved.getPetId(), saved.getStoreId(), saved.getServiceItemId(), saved.getStaffId(),
                saved.getServiceMode(), saved.getBookingDate(), saved.getStartTime(), saved.getEndTime(),
                saved.getAddressId(), saved.getDistanceKm(), saved.getContactName(), saved.getContactPhone(),
                saved.getPrice(), saved.getPaymentMethod(), saved.getPaymentStatus(),
                saved.getStatus(), saved.getRemark(), saved.getMerchantRemark(), saved.getCreateTime());
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

    private long countLogsForBooking(Long bookingId) {
        return bookingStatusLogService.count(
                new LambdaQueryWrapper<BookingStatusLog>()
                        .eq(BookingStatusLog::getBookingId, bookingId));
    }
}
