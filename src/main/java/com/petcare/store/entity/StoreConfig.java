package com.petcare.store.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for table `store_config`.
 * Does NOT extend BaseEntity because this table has no `deleted` field.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("store_config")
public class StoreConfig {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("store_id")
    private Long storeId;

    @TableField("home_service_radius_km")
    private BigDecimal homeServiceRadiusKm;

    @TableField("booking_advance_days")
    private Integer bookingAdvanceDays;

    @TableField("booking_cancel_hours")
    private Integer bookingCancelHours;

    @TableField("time_slot_minutes")
    private Integer timeSlotMinutes;

    @TableField("auto_confirm_booking")
    private Integer autoConfirmBooking;

    @TableField("content_auto_publish")
    private Integer contentAutoPublish;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
