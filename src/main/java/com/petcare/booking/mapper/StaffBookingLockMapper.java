package com.petcare.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.booking.entity.StaffBookingLock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface StaffBookingLockMapper extends BaseMapper<StaffBookingLock> {

    /**
     * Inserts a lock row or updates the existing one (upsert).
     * Ensures a stable lock point exists for (staffId, bookingDate).
     */
    int upsertStaffBookingLock(@Param("staffId") Long staffId,
                               @Param("bookingDate") LocalDate bookingDate);

    /**
     * Locks the (staffId, bookingDate) row for the current transaction.
     * Blocks until the lock is acquired or timeout.
     */
    StaffBookingLock selectStaffBookingLockForUpdate(@Param("staffId") Long staffId,
                                                     @Param("bookingDate") LocalDate bookingDate);
}
