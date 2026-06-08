package com.petcare.booking.service.impl;

import com.petcare.booking.entity.BookingStatusLog;
import com.petcare.booking.entity.ServiceBooking;
import com.petcare.booking.entity.StaffBookingLock;
import com.petcare.booking.mapper.ServiceBookingMapper;
import com.petcare.booking.mapper.StaffBookingLockMapper;
import com.petcare.booking.service.BookingStatusLogService;
import com.petcare.booking.service.BookingTransactionService;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Transactional booking operations.
 * Each public method is @Transactional on a separate bean
 * to avoid self-invocation proxy issues.
 */
@Service
public class BookingTransactionServiceImpl implements BookingTransactionService {

    private static final Logger log = LoggerFactory.getLogger(BookingTransactionServiceImpl.class);

    private final StaffBookingLockMapper staffBookingLockMapper;
    private final ServiceBookingMapper serviceBookingMapper;
    private final BookingStatusLogService bookingStatusLogService;

    public BookingTransactionServiceImpl(StaffBookingLockMapper staffBookingLockMapper,
                                         ServiceBookingMapper serviceBookingMapper,
                                         BookingStatusLogService bookingStatusLogService) {
        this.staffBookingLockMapper = staffBookingLockMapper;
        this.serviceBookingMapper = serviceBookingMapper;
        this.bookingStatusLogService = bookingStatusLogService;
    }

    @Override
    @Transactional
    public ServiceBooking createBookingOnce(ServiceBooking booking) {
        Long staffId = booking.getStaffId();
        LocalDate bookingDate = booking.getBookingDate();

        // Step 1: Ensure lock point exists with a unique id
        long lockId = generateLockId(staffId, bookingDate);
        staffBookingLockMapper.upsertStaffBookingLock(lockId, staffId, bookingDate);

        // Step 2: Lock the staff-date row
        StaffBookingLock lock = staffBookingLockMapper.selectStaffBookingLockForUpdate(staffId, bookingDate);
        if (lock == null) {
            throw new BusinessException(ErrorCode.BOOKING_STAFF_UNAVAILABLE,
                    "无法锁定员工排班，请重试");
        }

        // Step 3: Check for time conflicts
        List<ServiceBooking> conflicts = serviceBookingMapper.selectConflictingBookings(
                staffId, bookingDate, booking.getStartTime(), booking.getEndTime(), null);
        if (!conflicts.isEmpty()) {
            log.info("Booking time conflict: staffId={}, date={}, {}-{}, conflicts={}",
                    staffId, bookingDate, booking.getStartTime(), booking.getEndTime(), conflicts.size());
            throw new BusinessException(ErrorCode.BOOKING_TIME_CONFLICT,
                    "预约时间与已有预约冲突，请选择其他时间");
        }

        // Step 4: Insert booking
        serviceBookingMapper.insert(booking);

        // Step 5: Write status log
        writeStatusLog(booking.getId(), null, "PENDING_CONFIRM", "USER", booking.getUserId(), "创建预约");

        return booking;
    }

    @Override
    @Transactional
    public ServiceBooking reassignBookingOnce(Long bookingId, Long newStaffId,
                                              LocalDate bookingDate,
                                              LocalTime startTime, LocalTime endTime,
                                              Long operatorId) {
        // Step 1: Ensure lock point for new staff
        long lockId = generateLockId(newStaffId, bookingDate);
        staffBookingLockMapper.upsertStaffBookingLock(lockId, newStaffId, bookingDate);

        // Step 2: Lock new staff-date row
        StaffBookingLock lock = staffBookingLockMapper.selectStaffBookingLockForUpdate(newStaffId, bookingDate);
        if (lock == null) {
            throw new BusinessException(ErrorCode.BOOKING_STAFF_UNAVAILABLE,
                    "无法锁定新员工排班，请重试");
        }

        // Step 3: Check for conflicts with new staff (exclude current booking)
        List<ServiceBooking> conflicts = serviceBookingMapper.selectConflictingBookings(
                newStaffId, bookingDate, startTime, endTime, bookingId);
        if (!conflicts.isEmpty()) {
            throw new BusinessException(ErrorCode.BOOKING_TIME_CONFLICT,
                    "新员工在该时间段已有预约冲突，请选择其他员工或时间");
        }

        // Step 4: Update booking staff
        ServiceBooking booking = serviceBookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "预约不存在");
        }
        Long oldStaffId = booking.getStaffId();
        booking.setStaffId(newStaffId);
        serviceBookingMapper.updateById(booking);

        // Step 5: Write status log
        writeStatusLog(bookingId, booking.getStatus(), booking.getStatus(),
                "ADMIN", operatorId,
                String.format("改派员工：从员工%d改派到员工%d", oldStaffId, newStaffId));

        return booking;
    }

    private void writeStatusLog(Long bookingId, String oldStatus, String newStatus,
                                String operatorType, Long operatorId, String remark) {
        BookingStatusLog statusLog = new BookingStatusLog();
        statusLog.setBookingId(bookingId);
        statusLog.setOldStatus(oldStatus);
        statusLog.setNewStatus(newStatus);
        statusLog.setOperatorType(operatorType);
        statusLog.setOperatorId(operatorId);
        statusLog.setRemark(remark);
        bookingStatusLogService.save(statusLog);
    }

    /**
     * Generates a deterministic unique ID for the lock row.
     * Uses staffId * 100000 + dayOfYear to avoid collisions across dates.
     */
    private long generateLockId(Long staffId, LocalDate bookingDate) {
        return Math.abs(staffId * 100000L + bookingDate.getDayOfYear());
    }
}
