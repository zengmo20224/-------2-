package com.petcare.staff.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity for table `staff_skill`.
 * Does NOT extend BaseEntity because this table has no update_time or deleted field.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("staff_skill")
public class StaffSkill {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("staff_id")
    private Long staffId;

    @TableField("service_category_id")
    private Long serviceCategoryId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
