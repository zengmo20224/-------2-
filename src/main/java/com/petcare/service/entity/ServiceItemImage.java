package com.petcare.service.entity;

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
 * Entity for table `service_item_image`.
 * Does not extend BaseEntity because this table has no update_time or deleted field.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("service_item_image")
public class ServiceItemImage {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("service_item_id")
    private Long serviceItemId;

    @TableField("image_url")
    private String imageUrl;

    @TableField("sort")
    private Integer sort;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
