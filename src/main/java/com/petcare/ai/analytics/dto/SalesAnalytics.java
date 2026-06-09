package com.petcare.ai.analytics.dto;

import java.math.BigDecimal;

/**
 * Aggregated sales analytics data.
 */
public record SalesAnalytics(
        long totalOrders,
        long completedOrders,
        BigDecimal totalRevenue,
        BigDecimal avgOrderAmount,
        String topProductName,
        long topProductSalesQty
) {}
