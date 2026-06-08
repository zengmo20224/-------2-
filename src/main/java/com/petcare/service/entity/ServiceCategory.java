package com.petcare.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity for table `service_category`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("service_category")
public class ServiceCategory extends BaseEntity {

    @TableField("name")
    private String name;

    @TableField("icon_url")
    private String iconUrl;

    @TableField("sort")
    private Integer sort;

    @TableField("status")
    private String status;
}
