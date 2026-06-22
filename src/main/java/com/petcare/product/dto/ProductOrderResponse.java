package com.petcare.product.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for a product order (list view).
 */
public record ProductOrderResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        String orderNo,
        BigDecimal totalAmount,
        String deliveryMethod,
        String addressSnapshot,
        String paymentMethod,
        String paymentStatus,
        String pickupStatus,
        String status,
        String contactName,
        String contactPhone,
        String remark,
        LocalDateTime createTime,
        LocalDateTime confirmTime,
        LocalDateTime completeTime,
        LocalDateTime cancelTime
) {
}
