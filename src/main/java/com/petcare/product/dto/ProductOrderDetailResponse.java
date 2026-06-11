package com.petcare.product.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for a product order detail with items.
 */
public record ProductOrderDetailResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        String orderNo,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long userId,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long storeId,
        BigDecimal totalAmount,
        String paymentMethod,
        String paymentStatus,
        String pickupStatus,
        String status,
        String contactName,
        String contactPhone,
        String remark,
        String merchantRemark,
        LocalDateTime createTime,
        LocalDateTime confirmTime,
        LocalDateTime completeTime,
        LocalDateTime cancelTime,
        List<OrderItemResponse> items
) {
    /**
     * Response DTO for a single order item snapshot.
     */
    public record OrderItemResponse(
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long productId,
            String productName,
            String productCoverUrl,
            BigDecimal price,
            Integer quantity,
            BigDecimal totalAmount
    ) {
    }
}
