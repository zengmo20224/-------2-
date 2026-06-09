package com.petcare.product.dto;

/**
 * Request DTO for admin order actions (cancel, out-of-stock) with optional reason.
 */
public record AdminOrderActionRequest(
        String reason
) {
}
