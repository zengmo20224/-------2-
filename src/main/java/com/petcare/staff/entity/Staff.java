package com.petcare.staff.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity for table `staff`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("staff")
public class Staff extends BaseEntity {

    @TableField("store_id")
    private Long storeId;

    @TableField("name")
    private String name;

    @TableField("phone")
    private String phone;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("role")
    private String role;

    @TableField("status")
    private String status;

    @TableField("description")
    private String description;
}
