package com.petcare.booking.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entity for table `staff_unavailable_time`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("staff_unavailable_time")
public class StaffUnavailableTime extends BaseEntity {

    @TableField("staff_id")
    private Long staffId;

    @TableField("unavailable_date")
    private LocalDate unavailableDate;

    @TableField("start_time")
    private LocalTime startTime;

    @TableField("end_time")
    private LocalTime endTime;

    @TableField("reason_type")
    private String reasonType;

    @TableField("reason")
    private String reason;
}
