package com.petcare.ai.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for AI analysis report.
 */
public record AiAnalysisReportResponse(
        Long id,
        String reportType,
        LocalDate startDate,
        LocalDate endDate,
        String aiSummary,
        String suggestions,
        Long createdBy,
        LocalDateTime createTime
) {}
