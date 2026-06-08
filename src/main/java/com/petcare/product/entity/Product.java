package com.petcare.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entity for table `product`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("product")
public class Product extends BaseEntity {

    @TableField("category_id")
    private Long categoryId;

    @TableField("name")
    private String name;

    @TableField("cover_url")
    private String coverUrl;

    @TableField("price")
    private BigDecimal price;

    @TableField("stock")
    private Integer stock;

    @TableField("sales_count")
    private Integer salesCount;

    @TableField("description")
    private String description;

    @TableField("pickup_only")
    private Integer pickupOnly;

    @TableField("status")
    private String status;

    @TableField("sort")
    private Integer sort;
}
