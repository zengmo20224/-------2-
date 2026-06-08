package com.petcare.marketing.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity for table `marketing_activity`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("marketing_activity")
public class MarketingActivity extends BaseEntity {

    @TableField("title")
    private String title;

    @TableField("activity_type")
    private String activityType;

    @TableField("description")
    private String description;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("status")
    private String status;
}
