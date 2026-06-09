package com.petcare.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for a product order (list view).
 */
public record ProductOrderResponse(
        Long id,
        String orderNo,
        BigDecimal totalAmount,
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
