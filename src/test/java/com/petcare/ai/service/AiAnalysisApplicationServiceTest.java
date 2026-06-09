package com.petcare.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcare.ai.analytics.ActivityAnalyticsAggregator;
import com.petcare.ai.analytics.BusinessAnalyticsAggregator;
import com.petcare.ai.analytics.CommunityAnalyticsAggregator;
import com.petcare.ai.analytics.SalesAnalyticsAggregator;
import com.petcare.ai.analytics.dto.BusinessAnalytics;
import com.petcare.ai.dto.AiAnalysisCreateRequest;
import com.petcare.ai.dto.AiAnalysisReportResponse;
import com.petcare.ai.entity.AiAnalysisReport;
import com.petcare.ai.entity.AiUsageLog;
import com.petcare.ai.mapper.AiAnalysisReportMapper;
import com.petcare.ai.mapper.AiUsageLogMapper;
import com.petcare.ai.provider.AiProviderUnavailableException;
import com.petcare.ai.provider.MockAiProviderClient;
import com.petcare.ai.service.impl.AiAnalysisApplicationServiceImpl;
import com.petcare.admin.service.AdminOperationLogService;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for admin AI analysis application service.
 * Uses Mock Provider and mock aggregators.
 */
class AiAnalysisApplicationServiceTest {

    private AiAnalysisReportMapper reportMapper;
    private AiUsageLogMapper usageLogMapper;
    private AdminOperationLogService adminOperationLogService;
    private MockAiProviderClient mockProvider;
    private BusinessAnalyticsAggregator businessAggregator;
    private CommunityAnalyticsAggregator communityAggregator;
    private SalesAnalyticsAggregator salesAggregator;
    private ActivityAnalyticsAggregator activityAggregator;
    private ObjectMapper objectMapper;
    private AiAnalysisApplicationServiceImpl service;

    @BeforeEach
    void setUp() {
        reportMapper = mock(AiAnalysisReportMapper.class);
        usageLogMapper = mock(AiUsageLogMapper.class);
        adminOperationLogService = mock(AdminOperationLogService.class);
        mockProvider = new MockAiProviderClient();
        businessAggregator = mock(BusinessAnalyticsAggregator.class);
        communityAggregator = mock(CommunityAnalyticsAggregator.class);
        salesAggregator = mock(SalesAnalyticsAggregator.class);
        activityAggregator = mock(ActivityAnalyticsAggregator.class);
        objectMapper = new ObjectMapper();

        service = new AiAnalysisApplicationServiceImpl(
                reportMapper, usageLogMapper, adminOperationLogService, mockProvider,
                businessAggregator, communityAggregator, salesAggregator,
                activityAggregator, objectMapper
        );
    }

    @Nested
    @DisplayName("Date range validation")
    class DateRangeValidation {

        @Test
        @DisplayName("Start after end is rejected")
        void startAfterEnd_rejected() {
            AiAnalysisCreateRequest request = new AiAnalysisCreateRequest(
                    "BUSINESS", LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 1));
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.generateReport(1L, request));
            assertEquals(ErrorCode.AI_ANALYSIS_RANGE_INVALID, ex.getCode());
        }

        @Test
        @DisplayName("Future end date is rejected")
        void futureEndDate_rejected() {
            AiAnalysisCreateRequest request = new AiAnalysisCreateRequest(
                    "BUSINESS", LocalDate.of(2026, 6, 1), LocalDate.of(2099, 1, 1));
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.generateReport(1L, request));
            assertEquals(ErrorCode.AI_ANALYSIS_RANGE_INVALID, ex.getCode());
        }
    }

    @Nested
    @DisplayName("Report generation")
    class ReportGeneration {

        @Test
        @DisplayName("Generates BUSINESS report with aggregated data")
        void generatesBusinessReport() {
            when(businessAggregator.aggregate(any(), any()))
                    .thenReturn(new BusinessAnalytics(50, 30, 5, 15, null, null, 45.0));
            doAnswer(inv -> {
                AiAnalysisReport r = inv.getArgument(0);
                r.setId(1L);
                return 1;
            }).when(reportMapper).insert((AiAnalysisReport) any());
            mockProvider.withSuccess("本周预约量50次，完成率60%，建议增加高峰时段人手。");

            AiAnalysisCreateRequest request = new AiAnalysisCreateRequest(
                    "BUSINESS", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 7));
            AiAnalysisReportResponse response = service.generateReport(1L, request);

            assertNotNull(response);
            assertEquals("BUSINESS", response.reportType());
            assertTrue(response.aiSummary().contains("预约"));
            assertEquals(1, mockProvider.getCallCount());

            // Verify usage was logged
            verify(usageLogMapper).insert((AiUsageLog) any());
        }

        @Test
        @DisplayName("Provider failure does not save fake report")
        void providerFailure_noFakeReport() {
            when(businessAggregator.aggregate(any(), any()))
                    .thenReturn(new BusinessAnalytics(10, 5, 2, 3, null, null, 30.0));
            mockProvider.withUnavailable();

            AiAnalysisCreateRequest request = new AiAnalysisCreateRequest(
                    "BUSINESS", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 7));

            assertThrows(AiProviderUnavailableException.class,
                    () -> service.generateReport(1L, request));

            // Verify no report was saved
            verify(reportMapper, never()).insert((AiAnalysisReport) any());
            // Verify failed usage was logged
            verify(usageLogMapper).insert((AiUsageLog) any());
        }
    }
}
