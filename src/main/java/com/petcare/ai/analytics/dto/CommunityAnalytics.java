package com.petcare.ai.analytics.dto;

/**
 * Aggregated community analytics data.
 */
public record CommunityAnalytics(
        long totalPosts,
        long totalComments,
        long totalReports,
        long pendingReviewCount,
        long rejectedCount,
        String topTopicName,
        long topTopicPostCount
) {}
