package com.petcare.ai.analytics;

import com.petcare.ai.analytics.dto.CommunityAnalytics;
import com.petcare.community.mapper.PostMapper;
import com.petcare.community.mapper.PostCommentMapper;
import com.petcare.community.mapper.PostReportMapper;
import com.petcare.community.entity.Post;
import com.petcare.community.entity.PostComment;
import com.petcare.community.entity.PostReport;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDate;

/**
 * Aggregates community-related analytics.
 */
public class CommunityAnalyticsAggregator {

    private final PostMapper postMapper;
    private final PostCommentMapper postCommentMapper;
    private final PostReportMapper postReportMapper;

    public CommunityAnalyticsAggregator(
            PostMapper postMapper,
            PostCommentMapper postCommentMapper,
            PostReportMapper postReportMapper) {
        this.postMapper = postMapper;
        this.postCommentMapper = postCommentMapper;
        this.postReportMapper = postReportMapper;
    }

    /**
     * Aggregates community analytics for the given date range.
     */
    public CommunityAnalytics aggregate(LocalDate startDate, LocalDate endDate) {
        long totalPosts = postMapper.selectCount(
                new QueryWrapper<Post>()
                        .ge("create_time", startDate.atStartOfDay())
                        .le("create_time", endDate.plusDays(1).atStartOfDay())
                        .eq("deleted", 0));

        long totalComments = postCommentMapper.selectCount(
                new QueryWrapper<PostComment>()
                        .ge("create_time", startDate.atStartOfDay())
                        .le("create_time", endDate.plusDays(1).atStartOfDay())
                        .eq("deleted", 0));

        long totalReports = postReportMapper.selectCount(
                new QueryWrapper<PostReport>()
                        .ge("create_time", startDate.atStartOfDay())
                        .le("create_time", endDate.plusDays(1).atStartOfDay()));

        long pendingReviewCount = postMapper.selectCount(
                new QueryWrapper<Post>()
                        .eq("review_status", "PENDING")
                        .eq("deleted", 0));

        long rejectedCount = postMapper.selectCount(
                new QueryWrapper<Post>()
                        .eq("review_status", "REJECTED")
                        .eq("deleted", 0));

        return new CommunityAnalytics(
                totalPosts,
                totalComments,
                totalReports,
                pendingReviewCount,
                rejectedCount,
                null,
                0
        );
    }
}
