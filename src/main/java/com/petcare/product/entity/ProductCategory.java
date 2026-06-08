package com.petcare.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity for table `product_category`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("product_category")
public class ProductCategory extends BaseEntity {

    @TableField("name")
    private String name;

    @TableField("icon_url")
    private String iconUrl;

    @TableField("sort")
    private Integer sort;

    @TableField("status")
    private String status;
}
