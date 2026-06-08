package com.petcare.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for table `product_order`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("product_order")
public class ProductOrder extends BaseEntity {

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("store_id")
    private Long storeId;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("payment_method")
    private String paymentMethod;

    @TableField("payment_status")
    private String paymentStatus;

    @TableField("pickup_status")
    private String pickupStatus;

    @TableField("status")
    private String status;

    @TableField("contact_name")
    private String contactName;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("remark")
    private String remark;

    @TableField("merchant_remark")
    private String merchantRemark;

    @TableField("confirm_time")
    private LocalDateTime confirmTime;

    @TableField("complete_time")
    private LocalDateTime completeTime;

    @TableField("cancel_time")
    private LocalDateTime cancelTime;
}
