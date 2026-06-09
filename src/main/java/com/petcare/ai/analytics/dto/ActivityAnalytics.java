package com.petcare.ai.analytics.dto;

/**
 * Aggregated marketing activity analytics data.
 * When data is insufficient, fields are null or zero.
 * AI must not fabricate ROI when data is missing.
 */
public record ActivityAnalytics(
        long totalActivities,
        long activeActivities,
        Long associatedProductCount,
        Long associatedServiceCount,
        boolean dataSufficient
) {}
