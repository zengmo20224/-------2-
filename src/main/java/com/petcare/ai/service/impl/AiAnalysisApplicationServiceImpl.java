package com.petcare.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcare.ai.analytics.BusinessAnalyticsAggregator;
import com.petcare.ai.analytics.CommunityAnalyticsAggregator;
import com.petcare.ai.analytics.SalesAnalyticsAggregator;
import com.petcare.ai.analytics.ActivityAnalyticsAggregator;
import com.petcare.ai.domain.AiOutputSafetyPolicy;
import com.petcare.ai.domain.PromptFactory;
import com.petcare.ai.dto.AiAnalysisCreateRequest;
import com.petcare.ai.dto.AiAnalysisReportResponse;
import com.petcare.ai.entity.AiAnalysisReport;
import com.petcare.ai.entity.AiUsageLog;
import com.petcare.ai.mapper.AiAnalysisReportMapper;
import com.petcare.ai.mapper.AiUsageLogMapper;
import com.petcare.ai.provider.*;
import com.petcare.ai.service.AiAnalysisApplicationService;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.common.pagination.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of admin AI analysis application service.
 * Only uses backend-aggregated data — never lets AI generate SQL or query the database directly.
 */
@Service
public class AiAnalysisApplicationServiceImpl implements AiAnalysisApplicationService {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisApplicationServiceImpl.class);

    private final AiAnalysisReportMapper reportMapper;
    private final AiUsageLogMapper usageLogMapper;
    private final AiProviderClient providerClient;
    private final BusinessAnalyticsAggregator businessAnalyticsAggregator;
    private final CommunityAnalyticsAggregator communityAnalyticsAggregator;
    private final SalesAnalyticsAggregator salesAnalyticsAggregator;
    private final ActivityAnalyticsAggregator activityAnalyticsAggregator;
    private final ObjectMapper objectMapper;

    public AiAnalysisApplicationServiceImpl(
            AiAnalysisReportMapper reportMapper,
            AiUsageLogMapper usageLogMapper,
            AiProviderClient providerClient,
            BusinessAnalyticsAggregator businessAnalyticsAggregator,
            CommunityAnalyticsAggregator communityAnalyticsAggregator,
            SalesAnalyticsAggregator salesAnalyticsAggregator,
            ActivityAnalyticsAggregator activityAnalyticsAggregator,
            ObjectMapper objectMapper) {
        this.reportMapper = reportMapper;
        this.usageLogMapper = usageLogMapper;
        this.providerClient = providerClient;
        this.businessAnalyticsAggregator = businessAnalyticsAggregator;
        this.communityAnalyticsAggregator = communityAnalyticsAggregator;
        this.salesAnalyticsAggregator = salesAnalyticsAggregator;
        this.activityAnalyticsAggregator = activityAnalyticsAggregator;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public AiAnalysisReportResponse generateReport(Long adminId, AiAnalysisCreateRequest request) {
        validateDateRange(request.startDate(), request.endDate());

        // Step 1: Aggregate data from backend (read-only parameterized queries)
        String aggregatedDataJson = aggregateData(request.reportType(), request.startDate(), request.endDate());

        if (aggregatedDataJson == null) {
            throw new BusinessException(ErrorCode.AI_ANALYSIS_DATA_INSUFFICIENT, "数据不足，无法生成分析报告");
        }

        // Step 2: Call AI Provider with aggregated data only
        List<AiProviderMessage> messages = PromptFactory.buildAnalysisMessages(
                request.reportType(), aggregatedDataJson);

        AiProviderResponse response;
        try {
            response = providerClient.complete(
                    new AiProviderRequest(AiApiType.ANALYSIS, messages, null));
        } catch (AiProviderUnavailableException e) {
            logFailedUsage("provider_unavailable");
            throw e;
        } catch (AiProviderException e) {
            logFailedUsage(e.getInternalCode());
            throw e;
        }

        String output = response.assistantText();

        // Step 3: Output safety check
        if (AiOutputSafetyPolicy.isUnsafe(output)) {
            logFailedUsage("output_unsafe");
            throw new BusinessException(ErrorCode.AI_OUTPUT_REJECTED, "AI 输出未通过安全检查");
        }

        // Step 4: Save report in a short transaction
        AiAnalysisReport report = new AiAnalysisReport();
        report.setReportType(request.reportType());
        report.setStartDate(request.startDate());
        report.setEndDate(request.endDate());
        report.setRawDataJson(aggregatedDataJson);
        report.setAiSummary(output);
        report.setSuggestions(""); // AI summary includes suggestions in the response
        report.setCreatedBy(adminId);
        reportMapper.insert(report);

        // Step 5: Log successful usage
        logSuccessUsage(response);

        return toReportResponse(report);
    }

    @Override
    public PageResponse<AiAnalysisReportResponse> listReports(int page, int size, String reportType) {
        Page<AiAnalysisReport> pageParam = new Page<>(page, size);
        QueryWrapper<AiAnalysisReport> query = new QueryWrapper<AiAnalysisReport>()
                .orderByDesc("create_time");

        if (reportType != null && !reportType.isBlank()) {
            query.eq("report_type", reportType);
        }

        Page<AiAnalysisReport> result = reportMapper.selectPage(pageParam, query);

        List<AiAnalysisReportResponse> items = result.getRecords().stream()
                .map(this::toReportResponse)
                .toList();

        return PageResponse.of(items, result.getTotal(), page, size);
    }

    @Override
    public AiAnalysisReportResponse getReport(Long id) {
        AiAnalysisReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "报告不存在");
        }
        return toReportResponse(report);
    }

    private String aggregateData(String reportType, LocalDate startDate, LocalDate endDate) {
        try {
            return switch (reportType) {
                case "BUSINESS" -> objectMapper.writeValueAsString(
                        businessAnalyticsAggregator.aggregate(startDate, endDate));
                case "COMMUNITY" -> objectMapper.writeValueAsString(
                        communityAnalyticsAggregator.aggregate(startDate, endDate));
                case "SALES" -> objectMapper.writeValueAsString(
                        salesAnalyticsAggregator.aggregate(startDate, endDate));
                case "ACTIVITY" -> objectMapper.writeValueAsString(
                        activityAnalyticsAggregator.aggregate(startDate, endDate));
                default -> null;
            };
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize aggregated data", e);
            return null;
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException(ErrorCode.AI_ANALYSIS_RANGE_INVALID, "日期范围不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.AI_ANALYSIS_RANGE_INVALID, "开始日期不能晚于结束日期");
        }
        if (endDate.isAfter(LocalDate.now())) {
            throw new BusinessException(ErrorCode.AI_ANALYSIS_RANGE_INVALID, "结束日期不能超过今天");
        }
    }

    private void logSuccessUsage(AiProviderResponse response) {
        AiUsageLog usageLog = new AiUsageLog();
        usageLog.setApiType(AiApiType.ANALYSIS.name());
        usageLog.setModelName(response.modelName());
        if (response.usage() != null) {
            usageLog.setPromptTokens(response.usage().promptTokens());
            usageLog.setCompletionTokens(response.usage().completionTokens());
            usageLog.setTotalTokens(response.usage().totalTokens());
        }
        usageLog.setSuccess(1);
        usageLogMapper.insert(usageLog);
    }

    private void logFailedUsage(String errorCode) {
        try {
            AiUsageLog usageLog = new AiUsageLog();
            usageLog.setApiType(AiApiType.ANALYSIS.name());
            usageLog.setSuccess(0);
            usageLog.setErrorMessage(errorCode);
            usageLogMapper.insert(usageLog);
        } catch (Exception e) {
            log.warn("Failed to log AI usage: {}", e.getMessage());
        }
    }

    private AiAnalysisReportResponse toReportResponse(AiAnalysisReport r) {
        return new AiAnalysisReportResponse(
                r.getId(),
                r.getReportType(),
                r.getStartDate(),
                r.getEndDate(),
                r.getAiSummary(),
                r.getSuggestions(),
                r.getCreatedBy(),
                r.getCreateTime()
        );
    }
}
