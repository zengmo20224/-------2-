package com.petcare.product.service;

import com.petcare.product.dto.ProductOrderCreateRequest;
import com.petcare.product.dto.ProductOrderDetailResponse;
import com.petcare.product.dto.ProductOrderResponse;
import com.petcare.common.pagination.PageResponse;

/**
 * Application service for user-facing product order operations.
 */
public interface ProductOrderApplicationService {

    /**
     * Creates a pickup order from the current user's checked cart items.
     * currentUserId comes from the security context, not the request body.
     */
    ProductOrderResponse createOrder(Long currentUserId, ProductOrderCreateRequest request);

    /**
     * Lists the current user's orders, paginated.
     */
    PageResponse<ProductOrderResponse> getMyOrders(Long currentUserId, int page, int size);

    /**
     * Gets order detail. Verifies ownership for non-admin access.
     */
    ProductOrderDetailResponse getOrderDetail(Long currentUserId, Long orderId);

    /**
     * Cancels a PENDING_CONFIRM order. Verifies ownership.
     */
    ProductOrderResponse cancelOrder(Long currentUserId, Long orderId);
}
