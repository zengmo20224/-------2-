package com.petcare.store.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entity for table `store`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("store")
public class Store extends BaseEntity {

    @TableField("store_name")
    private String storeName;

    @TableField("phone")
    private String phone;

    @TableField("address")
    private String address;

    @TableField("longitude")
    private BigDecimal longitude;

    @TableField("latitude")
    private BigDecimal latitude;

    @TableField("business_hours")
    private String businessHours;

    @TableField("status")
    private String status;

    @TableField("description")
    private String description;
}
