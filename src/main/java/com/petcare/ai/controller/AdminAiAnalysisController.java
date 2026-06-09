package com.petcare.ai.controller;

import com.petcare.ai.dto.AiAnalysisCreateRequest;
import com.petcare.ai.dto.AiAnalysisReportResponse;
import com.petcare.ai.service.AiAnalysisApplicationService;
import com.petcare.common.api.ApiResponse;
import com.petcare.common.pagination.PageResponse;
import com.petcare.common.security.SecurityContextHelper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin AI analysis report endpoints.
 * All endpoints require authentication and specific permission codes.
 */
@RestController
@RequestMapping("/api/v1/admin/ai/analysis-reports")
public class AdminAiAnalysisController {

    private final AiAnalysisApplicationService analysisService;

    public AdminAiAnalysisController(AiAnalysisApplicationService analysisService) {
        this.analysisService = analysisService;
    }

    /**
     * Generate an AI analysis report.
     * Requires ai:analysis:generate permission.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ai:analysis:generate')")
    public ResponseEntity<ApiResponse<AiAnalysisReportResponse>> generateReport(
            @Valid @RequestBody AiAnalysisCreateRequest request) {
        Long adminId = SecurityContextHelper.getCurrentAdminId()
                .orElseThrow(() -> new IllegalStateException("No admin identity"));
        AiAnalysisReportResponse response = analysisService.generateReport(adminId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * List analysis reports, paginated with optional type filter.
     * Requires analytics:dashboard:read permission.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('analytics:dashboard:read')")
    public ResponseEntity<ApiResponse<PageResponse<AiAnalysisReportResponse>>> listReports(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String reportType) {
        PageResponse<AiAnalysisReportResponse> response = analysisService.listReports(page, size, reportType);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get a single analysis report by ID.
     * Requires analytics:dashboard:read permission.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('analytics:dashboard:read')")
    public ResponseEntity<ApiResponse<AiAnalysisReportResponse>> getReport(@PathVariable Long id) {
        AiAnalysisReportResponse response = analysisService.getReport(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
