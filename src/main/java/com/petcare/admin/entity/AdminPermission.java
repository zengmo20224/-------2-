package com.petcare.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity for table `admin_permission`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("admin_permission")
public class AdminPermission extends BaseEntity {

    @TableField("permission_code")
    private String permissionCode;

    @TableField("permission_name")
    private String permissionName;

    @TableField("module")
    private String module;

    @TableField("description")
    private String description;

    @TableField("status")
    private String status;
}
