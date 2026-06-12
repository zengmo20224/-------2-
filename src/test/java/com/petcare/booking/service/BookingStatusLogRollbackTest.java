package com.petcare.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.booking.entity.BookingStatusLog;
import com.petcare.booking.entity.ServiceBooking;
import com.petcare.booking.entity.StaffSchedule;
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
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;

/**
 * Verifies that a status-log write failure rolls back the booking status change (RM-B02).
 *
 * <p>Uses {@code @SpyBean} so that {@code count()} queries hit the real database.
 * Only {@code save()} calls for CONFIRMED logs are made to throw; other saves
 * (e.g. the initial create log) proceed normally.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("status-transition")
class BookingStatusLogRollbackTest {

    @Autowired private BookingApplicationService bookingApplicationService;
    @Autowired private ServiceBookingService serviceBookingService;
    @SpyBean private BookingStatusLogService bookingStatusLogService;
    @Autowired private StoreService storeService;
    @Autowired private StoreConfigService storeConfigService;
    @Autowired private ServiceCategoryService serviceCategoryService;
    @Autowired private ServiceItemService serviceItemService;
    @Autowired private StaffService staffService;
    @Autowired private StaffSkillService staffSkillService;
    @Autowired private StaffScheduleService staffScheduleService;

    private Long storeId;
    private Long serviceItemId;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        futureDate = LocalDate.now().plusDays(3);

        Store store = new Store();
        store.setStoreName("日志回滚测试门店");
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
        cat.setName("日志回滚测试分类");
        cat.setSort(1);
        cat.setStatus("ACTIVE");
        serviceCategoryService.save(cat);

        ServiceItem item = new ServiceItem();
        item.setCategoryId(cat.getId());
        item.setName("日志回滚测试服务");
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

        Long staffId = createStaffWithSkill("日志回滚测试员工", storeId, cat.getId());
        createSchedule(staffId, storeId, futureDate);
    }

    @Test
    @DisplayName("Status log write failure rolls back booking status change")
    void statusLogFailure_rollsBackBookingStatus() {
        // Make save() throw ONLY when the log targets CONFIRMED status.
        // Other saves (e.g. initial PENDING_CONFIRM log) proceed normally.
        doThrow(new RuntimeException("Simulated log write failure"))
                .when(bookingStatusLogService).save(
                        argThat(log -> log != null && "CONFIRMED".equals(log.getNewStatus())));

        // Create booking directly (bypasses createBookingOnce which also writes a log)
        ServiceBooking booking = new ServiceBooking();
        booking.setBookingNo("BK_LOGROLLBACK_" + System.nanoTime());
        booking.setUserId(91001L);
        booking.setStoreId(storeId);
        booking.setServiceItemId(serviceItemId);
        booking.setStaffId(getFirstStaffId());
        booking.setBookingDate(futureDate);
        booking.setStartTime(LocalTime.of(14, 0));
        booking.setEndTime(LocalTime.of(15, 0));
        booking.setServiceMode("STORE");
        booking.setStatus("PENDING_CONFIRM");
        booking.setContactName("日志回滚测试用户");
        booking.setContactPhone("13800910001");
        serviceBookingService.save(booking);

        // Attempt confirm — should fail because CONFIRMED log save throws
        assertThatThrownBy(() ->
                bookingApplicationService.confirmBooking(booking.getId(), null, 9999L))
                .isInstanceOf(Exception.class);

        // Verify booking status is still PENDING_CONFIRM (rolled back)
        ServiceBooking reloaded = serviceBookingService.getById(booking.getId());
        assertThat(reloaded.getStatus())
                .as("Booking status must remain PENDING_CONFIRM after log write failure")
                .isEqualTo("PENDING_CONFIRM");

        // Verify no CONFIRMED log in the real database (count hits real DB via SpyBean)
        long confirmLogs = bookingStatusLogService.count(
                new LambdaQueryWrapper<BookingStatusLog>()
                        .eq(BookingStatusLog::getBookingId, booking.getId())
                        .eq(BookingStatusLog::getNewStatus, "CONFIRMED"));
        assertThat(confirmLogs)
                .as("No CONFIRMED status log should exist in DB after rollback")
                .isEqualTo(0);
    }

    // --- Helper methods ---

    private Long getFirstStaffId() {
        return staffService.list().get(0).getId();
    }

    private Long createStaffWithSkill(String name, Long sStoreId, Long categoryId) {
        Staff staff = new Staff();
        staff.setStoreId(sStoreId);
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

    private void createSchedule(Long staffId, Long sStoreId, LocalDate date) {
        StaffSchedule schedule = new StaffSchedule();
        schedule.setStaffId(staffId);
        schedule.setStoreId(sStoreId);
        schedule.setWorkDate(date);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(17, 0));
        schedule.setStatus("AVAILABLE");
        staffScheduleService.save(schedule);
    }
}
