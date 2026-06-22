package com.petcare.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity for table `product_carousel_image`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("product_carousel_image")
public class ProductCarouselImage extends BaseEntity {

    @TableField("title")
    private String title;

    @TableField("image_url")
    private String imageUrl;

    @TableField("link_type")
    private String linkType;

    @TableField("link_target_id")
    private Long linkTargetId;

    @TableField("status")
    private String status;

    @TableField("sort")
    private Integer sort;
}
