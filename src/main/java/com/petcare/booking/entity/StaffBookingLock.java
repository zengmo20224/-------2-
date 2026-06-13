package com.petcare.booking.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity for table `staff_booking_lock`.
 * Does NOT extend BaseEntity because this table has no deleted field.
 * Used for booking concurrency control (see docs/01-architecture-design.md).
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("staff_booking_lock")
public class StaffBookingLock {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("staff_id")
    private Long staffId;

    @TableField("booking_date")
    private LocalDate bookingDate;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
