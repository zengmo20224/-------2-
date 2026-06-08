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
 * Entity for table `staff_schedule`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("staff_schedule")
public class StaffSchedule extends BaseEntity {

    @TableField("staff_id")
    private Long staffId;

    @TableField("store_id")
    private Long storeId;

    @TableField("work_date")
    private LocalDate workDate;

    @TableField("start_time")
    private LocalTime startTime;

    @TableField("end_time")
    private LocalTime endTime;

    @TableField("status")
    private String status;

    @TableField("remark")
    private String remark;
}
