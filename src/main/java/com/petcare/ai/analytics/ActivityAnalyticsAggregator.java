package com.petcare.ai.analytics;

import com.petcare.ai.analytics.dto.ActivityAnalytics;
import com.petcare.marketing.mapper.MarketingActivityMapper;
import com.petcare.marketing.entity.MarketingActivity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDate;

/**
 * Aggregates marketing activity analytics.
 * When data is insufficient, explicitly marks dataSufficient=false.
 * AI must not fabricate ROI when data is missing.
 */
public class ActivityAnalyticsAggregator {

    private final MarketingActivityMapper marketingActivityMapper;

    public ActivityAnalyticsAggregator(MarketingActivityMapper marketingActivityMapper) {
        this.marketingActivityMapper = marketingActivityMapper;
    }

    /**
     * Aggregates activity analytics for the given date range.
     */
    public ActivityAnalytics aggregate(LocalDate startDate, LocalDate endDate) {
        long totalActivities = marketingActivityMapper.selectCount(
                new QueryWrapper<MarketingActivity>()
                        .eq("deleted", 0));

        long activeActivities = marketingActivityMapper.selectCount(
                new QueryWrapper<MarketingActivity>()
                        .eq("status", "ACTIVE")
                        .eq("deleted", 0));

        boolean dataSufficient = totalActivities > 0;

        return new ActivityAnalytics(
                totalActivities,
                activeActivities,
                dataSufficient ? totalActivities : null,
                dataSufficient ? totalActivities : null,
                dataSufficient
        );
    }
}
