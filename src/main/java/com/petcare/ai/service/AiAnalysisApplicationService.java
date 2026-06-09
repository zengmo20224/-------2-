package com.petcare.ai.service;

import com.petcare.ai.dto.AiAnalysisCreateRequest;
import com.petcare.ai.dto.AiAnalysisReportResponse;
import com.petcare.common.pagination.PageResponse;

/**
 * Application service for admin AI analysis report operations.
 * Only accepts aggregated data from backend — never raw SQL or database queries.
 */
public interface AiAnalysisApplicationService {

    /**
     * Generates an AI analysis report based on backend-aggregated data.
     * Requires ai:analysis:generate permission.
     *
     * @param adminId  the admin ID from security context
     * @param request  analysis creation request with report type and date range
     * @return generated analysis report
     */
    AiAnalysisReportResponse generateReport(Long adminId, AiAnalysisCreateRequest request);

    /**
     * Lists analysis reports, paginated.
     * Requires analytics:dashboard:read permission.
     */
    PageResponse<AiAnalysisReportResponse> listReports(int page, int size, String reportType);

    /**
     * Gets a single analysis report by ID.
     * Requires analytics:dashboard:read permission.
     */
    AiAnalysisReportResponse getReport(Long id);
}
