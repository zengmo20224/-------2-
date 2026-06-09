package com.petcare.ai.analytics;

import com.petcare.ai.analytics.dto.SalesAnalytics;
import com.petcare.product.mapper.ProductOrderMapper;
import com.petcare.product.entity.ProductOrder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Aggregates sales-related analytics from product orders.
 */
public class SalesAnalyticsAggregator {

    private final ProductOrderMapper productOrderMapper;

    public SalesAnalyticsAggregator(ProductOrderMapper productOrderMapper) {
        this.productOrderMapper = productOrderMapper;
    }

    /**
     * Aggregates sales analytics for the given date range.
     */
    public SalesAnalytics aggregate(LocalDate startDate, LocalDate endDate) {
        long totalOrders = productOrderMapper.selectCount(
                new QueryWrapper<ProductOrder>()
                        .ge("create_time", startDate.atStartOfDay())
                        .le("create_time", endDate.plusDays(1).atStartOfDay())
                        .eq("deleted", 0));

        long completedOrders = productOrderMapper.selectCount(
                new QueryWrapper<ProductOrder>()
                        .ge("create_time", startDate.atStartOfDay())
                        .le("create_time", endDate.plusDays(1).atStartOfDay())
                        .eq("status", "COMPLETED")
                        .eq("deleted", 0));

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal avgOrderAmount = BigDecimal.ZERO;

        return new SalesAnalytics(
                totalOrders,
                completedOrders,
                totalRevenue,
                avgOrderAmount,
                null,
                0
        );
    }
}
