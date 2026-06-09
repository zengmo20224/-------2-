package com.petcare.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for a product order detail with items.
 */
public record ProductOrderDetailResponse(
        Long id,
        String orderNo,
        Long userId,
        Long storeId,
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
            Long id,
            Long productId,
            String productName,
            String productCoverUrl,
            BigDecimal price,
            Integer quantity,
            BigDecimal totalAmount
    ) {
    }
}
