package com.petcare.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entity for table `service_item`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("service_item")
public class ServiceItem extends BaseEntity {

    @TableField("category_id")
    private Long categoryId;

    @TableField("name")
    private String name;

    @TableField("service_mode")
    private String serviceMode;

    @TableField("price")
    private BigDecimal price;

    @TableField("duration_minutes")
    private Integer durationMinutes;

    @TableField("pet_type")
    private String petType;

    @TableField("pet_size")
    private String petSize;

    @TableField("need_address")
    private Integer needAddress;

    @TableField("need_pet")
    private Integer needPet;

    @TableField("description")
    private String description;

    @TableField("cover_url")
    private String coverUrl;

    @TableField("status")
    private String status;

    @TableField("sort")
    private Integer sort;
}
