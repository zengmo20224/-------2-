package com.petcare.product.service;

import com.petcare.product.dto.ProductOrderDetailResponse;
import com.petcare.product.dto.ProductOrderResponse;
import com.petcare.common.pagination.PageResponse;

/**
 * Application service for admin product order operations.
 */
public interface AdminProductOrderService {

    /**
     * Lists all orders with optional filters, paginated.
     */
    PageResponse<ProductOrderResponse> listOrders(int page, int size, String status);

    /**
     * Gets order detail by ID (admin access, no ownership check).
     */
    ProductOrderDetailResponse getOrderDetail(Long orderId);

    /**
     * Confirms a PENDING_CONFIRM order → PREPARING.
     */
    ProductOrderResponse confirmOrder(Long orderId, Long operatorId);

    /**
     * Marks PREPARING order as READY_FOR_PICKUP.
     */
    ProductOrderResponse markReadyForPickup(Long orderId, Long operatorId);

    /**
     * Confirms offline payment for READY_FOR_PICKUP order.
     */
    ProductOrderResponse confirmPayment(Long orderId, Long operatorId);

    /**
     * Completes an order. Requires OFFLINE_PAID and PICKED_UP.
     */
    ProductOrderResponse completeOrder(Long orderId, Long operatorId);

    /**
     * Cancels an order (admin). Restores stock.
     */
    ProductOrderResponse cancelOrder(Long orderId, String reason, Long operatorId);

    /**
     * Marks order as OUT_OF_STOCK. Does NOT restore stock.
     */
    ProductOrderResponse outOfStock(Long orderId, String reason, Long operatorId);
}
