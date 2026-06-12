package com.petcare.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.admin.entity.AdminOperationLog;
import com.petcare.admin.service.AdminOperationLogService;
import com.petcare.booking.dto.BookingCancelRequest;
import com.petcare.booking.dto.BookingReassignRequest;
import com.petcare.booking.dto.BookingResponse;
import com.petcare.booking.entity.ServiceBooking;
import com.petcare.booking.service.impl.BookingApplicationServiceImpl;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.service.entity.ServiceItem;
import com.petcare.service.service.ServiceItemService;
import com.petcare.staff.entity.Staff;
import com.petcare.staff.entity.StaffSkill;
import com.petcare.staff.service.StaffService;
import com.petcare.staff.service.StaffSkillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mockito unit tests for booking admin operation audit logging.
 *
 * Verifies:
 * - SUCCESS audit is written with correct fields on successful operations
 * - FAIL audit uses REQUIRES_NEW via saveFailLog on business failure
 * - Unknown exceptions are sanitized to "unexpected_error"
 * - Fail audit failure does not suppress original business exception
 * - User cancel does NOT write admin operation log
 * - All 6 operations map to correct audit operation values and URLs
 */
@ExtendWith(MockitoExtension.class)
class BookingAdminAuditTest {

    @Mock private ServiceItemService serviceItemService;
    @Mock private com.petcare.store.service.StoreConfigService storeConfigService;
    @Mock private com.petcare.store.service.StoreService storeService;
    @Mock private StaffSkillService staffSkillService;
    @Mock private StaffService staffService;
    @Mock private StaffScheduleService staffScheduleService;
    @Mock private StaffUnavailableTimeService staffUnavailableTimeService;
    @Mock private ServiceBookingService serviceBookingService;
    @Mock private BookingStatusLogService bookingStatusLogService;
    @Mock private com.petcare.user.service.UserAddressService userAddressService;
    @Mock private BookingRetryService bookingRetryService;
    @Mock private BookingTransactionService bookingTransactionService;
    @Mock private AdminOperationLogService operationLogService;

    private BookingApplicationServiceImpl service;

    private static final Long BOOKING_ID = 1001L;
    private static final Long OPERATOR_ID = 2001L;
    private static final Long STAFF_ID = 3001L;
    private static final Long NEW_STAFF_ID = 3002L;

    @BeforeEach
    void setUp() {
        service = new BookingApplicationServiceImpl(
                serviceItemService, storeConfigService, storeService,
                staffSkillService, staffService, staffScheduleService,
                staffUnavailableTimeService, serviceBookingService,
                bookingStatusLogService, userAddressService,
                bookingRetryService, bookingTransactionService,
                operationLogService);
    }

    private ServiceBooking stubBooking() {
        ServiceBooking b = new ServiceBooking();
        b.setId(BOOKING_ID);
        b.setBookingNo("BK001");
        b.setUserId(4001L);
        b.setStoreId(5001L);
        b.setServiceItemId(6001L);
        b.setStaffId(STAFF_ID);
        b.setServiceMode("STORE");
        b.setBookingDate(LocalDate.now().plusDays(3));
        b.setStartTime(LocalTime.of(10, 0));
        b.setEndTime(LocalTime.of(11, 0));
        b.setStatus("PENDING_CONFIRM");
        b.setPrice(new BigDecimal("99.00"));
        b.setPaymentStatus("UNPAID");
        b.setCreateTime(LocalDateTime.now());
        return b;
    }

    // ========== SUCCESS AUDIT TESTS ==========

    @Nested
    @DisplayName("SUCCESS audit")
    class SuccessAudit {

        @Test
        @DisplayName("Confirm success writes SUCCESS audit with admin ID, booking ID, path")
        void confirmSuccess_writesSuccessAudit() {
            ServiceBooking booking = stubBooking();
            booking.setStatus("CONFIRMED");
            when(bookingTransactionService.transitionStatusOnce(
                    BOOKING_ID, "CONFIRMED", "ADMIN", OPERATOR_ID,
                    "管理员确认预约", null, "备注"))
                    .thenReturn(booking);
            when(operationLogService.save(any(AdminOperationLog.class))).thenReturn(true);

            service.confirmBooking(BOOKING_ID, "备注", OPERATOR_ID);

            ArgumentCaptor<AdminOperationLog> captor = ArgumentCaptor.forClass(AdminOperationLog.class);
            verify(operationLogService).save(captor.capture());

            AdminOperationLog log = captor.getValue();
            assertThat(log.getAdminId()).isEqualTo(OPERATOR_ID);
            assertThat(log.getModule()).isEqualTo("booking");
            assertThat(log.getOperation()).isEqualTo("confirm-booking");
            assertThat(log.getRequestMethod()).isEqualTo("POST");
            assertThat(log.getRequestUrl()).isEqualTo("/api/v1/admin/bookings/" + BOOKING_ID + "/confirm");
            assertThat(log.getRequestParams()).contains(String.valueOf(BOOKING_ID));
            assertThat(log.getResult()).isEqualTo("SUCCESS");
            assertThat(log.getErrorMessage()).isNull();
        }

        @Test
        @DisplayName("Reassign success writes audit with booking ID and new staff ID")
        void reassignSuccess_writesAuditInBusinessTransaction() {
            ServiceBooking booking = stubBooking();
            booking.setStatus("CONFIRMED");
            when(serviceBookingService.getById(BOOKING_ID)).thenReturn(booking);
            when(serviceItemService.getById(booking.getServiceItemId())).thenReturn(new ServiceItem());
            when(staffSkillService.getOne(any(LambdaQueryWrapper.class))).thenReturn(new StaffSkill());
            Staff newStaff = new Staff();
            newStaff.setId(NEW_STAFF_ID);
            newStaff.setStatus("ACTIVE");
            when(staffService.getById(NEW_STAFF_ID)).thenReturn(newStaff);
            when(bookingTransactionService.reassignBookingOnce(
                    BOOKING_ID, NEW_STAFF_ID, booking.getBookingDate(),
                    booking.getStartTime(), booking.getEndTime(), OPERATOR_ID))
                    .thenReturn(booking);
            when(operationLogService.save(any(AdminOperationLog.class))).thenReturn(true);

            service.reassignBooking(BOOKING_ID,
                    new BookingReassignRequest(NEW_STAFF_ID), OPERATOR_ID);

            ArgumentCaptor<AdminOperationLog> captor = ArgumentCaptor.forClass(AdminOperationLog.class);
            verify(operationLogService).save(captor.capture());

            AdminOperationLog log = captor.getValue();
            assertThat(log.getOperation()).isEqualTo("reassign-booking");
            assertThat(log.getRequestUrl()).isEqualTo("/api/v1/admin/bookings/" + BOOKING_ID + "/reassign");
            assertThat(log.getRequestParams()).contains(String.valueOf(BOOKING_ID));
            assertThat(log.getRequestParams()).contains(String.valueOf(NEW_STAFF_ID));
            assertThat(log.getResult()).isEqualTo("SUCCESS");
        }
    }

    // ========== FAIL AUDIT TESTS ==========

    @Nested
    @DisplayName("FAIL audit")
    class FailAudit {

        @Test
        @DisplayName("Business failure writes one FAIL audit and preserves original exception")
        void businessFailure_writesOneFailAudit() {
            BusinessException original = new BusinessException(
                    ErrorCode.BOOKING_STATUS_INVALID, "预约状态不允许此操作");
            when(bookingTransactionService.transitionStatusOnce(
                    BOOKING_ID, "CONFIRMED", "ADMIN", OPERATOR_ID,
                    "管理员确认预约", null, null))
                    .thenThrow(original);
            when(operationLogService.saveFailLog(any(AdminOperationLog.class))).thenReturn(true);

            assertThatThrownBy(() -> service.confirmBooking(BOOKING_ID, null, OPERATOR_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("预约状态不允许此操作");

            ArgumentCaptor<AdminOperationLog> captor = ArgumentCaptor.forClass(AdminOperationLog.class);
            verify(operationLogService).saveFailLog(captor.capture());
            verify(operationLogService, never()).save(any());

            AdminOperationLog log = captor.getValue();
            assertThat(log.getResult()).isEqualTo("FAIL");
            assertThat(log.getErrorMessage()).isEqualTo("预约状态不允许此操作");
            assertThat(log.getAdminId()).isEqualTo(OPERATOR_ID);
        }

        @Test
        @DisplayName("Unknown exception writes sanitized 'unexpected_error' in audit")
        void unexpectedFailure_writesSanitizedFailAudit() {
            RuntimeException unexpected = new RuntimeException("SQL: SELECT * FROM users; stack=...");
            when(bookingTransactionService.transitionStatusOnce(
                    BOOKING_ID, "CONFIRMED", "ADMIN", OPERATOR_ID,
                    "管理员确认预约", null, null))
                    .thenThrow(unexpected);
            when(operationLogService.saveFailLog(any(AdminOperationLog.class))).thenReturn(true);

            assertThatThrownBy(() -> service.confirmBooking(BOOKING_ID, null, OPERATOR_ID))
                    .isInstanceOf(RuntimeException.class);

            ArgumentCaptor<AdminOperationLog> captor = ArgumentCaptor.forClass(AdminOperationLog.class);
            verify(operationLogService).saveFailLog(captor.capture());

            AdminOperationLog log = captor.getValue();
            assertThat(log.getErrorMessage()).isEqualTo("unexpected_error");
        }

        @Test
        @DisplayName("Fail audit write failure preserves original business exception")
        void failAuditFailure_preservesOriginalException() {
            BusinessException original = new BusinessException(
                    ErrorCode.BOOKING_STATUS_INVALID, "状态无效");
            when(bookingTransactionService.transitionStatusOnce(
                    BOOKING_ID, "CONFIRMED", "ADMIN", OPERATOR_ID,
                    "管理员确认预约", null, null))
                    .thenThrow(original);
            when(operationLogService.saveFailLog(any(AdminOperationLog.class)))
                    .thenThrow(new RuntimeException("DB connection lost"));

            assertThatThrownBy(() -> service.confirmBooking(BOOKING_ID, null, OPERATOR_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("状态无效");
        }

        @Test
        @DisplayName("Fail audit false result preserves original business exception")
        void failAuditFalseResult_preservesOriginalException() {
            BusinessException original = new BusinessException(
                    ErrorCode.BOOKING_STATUS_INVALID, "状态无效");
            when(bookingTransactionService.transitionStatusOnce(
                    BOOKING_ID, "CONFIRMED", "ADMIN", OPERATOR_ID,
                    "管理员确认预约", null, null))
                    .thenThrow(original);
            when(operationLogService.saveFailLog(any(AdminOperationLog.class))).thenReturn(false);

            assertThatThrownBy(() -> service.confirmBooking(BOOKING_ID, null, OPERATOR_ID))
                    .isSameAs(original);
        }
    }

    // ========== USER CANCEL TEST ==========

    @Test
    @DisplayName("User cancel does not write admin operation audit")
    void userCancel_doesNotWriteAdminAudit() {
        ServiceBooking booking = stubBooking();
        when(serviceBookingService.getById(BOOKING_ID)).thenReturn(booking);
        when(storeConfigService.getOne(any(LambdaQueryWrapper.class)))
                .thenReturn(stubStoreConfig());
        ServiceBooking cancelled = stubBooking();
        cancelled.setStatus("CANCELLED");
        when(bookingTransactionService.transitionStatusOnce(
                BOOKING_ID, "CANCELLED", "USER", 4001L, "用户取消预约",
                "不想要了", null))
                .thenReturn(cancelled);

        service.cancelBooking(4001L, BOOKING_ID, new BookingCancelRequest("不想要了"));

        verify(operationLogService, never()).save(any());
        verify(operationLogService, never()).saveFailLog(any());
    }

    // ========== PARAMETERIZED OPERATION MAPPING TESTS ==========

    @ParameterizedTest(name = "{0} maps to operation={1}")
    @CsvSource({
            "REJECTED, reject-booking, /reject",
            "IN_SERVICE, start-booking, /start",
            "COMPLETED, complete-booking, /complete",
            "CANCELLED, cancel-booking, /cancel"
    })
    @DisplayName("Admin operations map to correct audit operation and URL")
    void adminOperation_writesCorrectAudit(String targetStatus, String expectedOperation, String urlSuffix) {
        // Setup a confirmed booking for start/complete/cancel
        ServiceBooking booking = stubBooking();
        if ("IN_SERVICE".equals(targetStatus) || "COMPLETED".equals(targetStatus)) {
            booking.setStatus("CONFIRMED");
        }
        ServiceBooking updated = stubBooking();
        updated.setStatus(targetStatus);

        when(bookingTransactionService.transitionStatusOnce(
                any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(updated);
        when(operationLogService.save(any(AdminOperationLog.class))).thenReturn(true);

        switch (targetStatus) {
            case "REJECTED" -> service.rejectBooking(BOOKING_ID, "原因", OPERATOR_ID);
            case "IN_SERVICE" -> service.startBooking(BOOKING_ID, OPERATOR_ID);
            case "COMPLETED" -> service.completeBooking(BOOKING_ID, OPERATOR_ID);
            case "CANCELLED" -> service.cancelBookingAdmin(BOOKING_ID, "原因", OPERATOR_ID);
        }

        ArgumentCaptor<AdminOperationLog> captor = ArgumentCaptor.forClass(AdminOperationLog.class);
        verify(operationLogService).save(captor.capture());

        AdminOperationLog log = captor.getValue();
        assertThat(log.getOperation()).isEqualTo(expectedOperation);
        assertThat(log.getRequestUrl()).isEqualTo("/api/v1/admin/bookings/" + BOOKING_ID + urlSuffix);
    }

    @Test
    @DisplayName("Success audit save failure throws IllegalStateException")
    void successAuditSaveReturnsFalse_throwsIllegalStateException() {
        ServiceBooking booking = stubBooking();
        booking.setStatus("CONFIRMED");
        when(bookingTransactionService.transitionStatusOnce(
                BOOKING_ID, "CONFIRMED", "ADMIN", OPERATOR_ID,
                "管理员确认预约", null, null))
                .thenReturn(booking);
        when(operationLogService.save(any(AdminOperationLog.class))).thenReturn(false);

        assertThatThrownBy(() -> service.confirmBooking(BOOKING_ID, null, OPERATOR_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("admin operation log");
    }

    // ========== HELPERS ==========

    private com.petcare.store.entity.StoreConfig stubStoreConfig() {
        com.petcare.store.entity.StoreConfig config = new com.petcare.store.entity.StoreConfig();
        config.setStoreId(5001L);
        config.setHomeServiceRadiusKm(new BigDecimal("5.00"));
        config.setBookingAdvanceDays(14);
        config.setBookingCancelHours(4);
        config.setTimeSlotMinutes(30);
        return config;
    }
}
