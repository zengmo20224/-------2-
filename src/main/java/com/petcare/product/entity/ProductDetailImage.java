package com.petcare.product.entity;

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
 * Entity for table `product_detail_image`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("product_detail_image")
public class ProductDetailImage {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("product_id")
    private Long productId;

    @TableField("image_url")
    private String imageUrl;

    @TableField("sort")
    private Integer sort;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
