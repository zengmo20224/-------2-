package com.petcare.ai.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for AI analysis report.
 */
public record AiAnalysisReportResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        String reportType,
        LocalDate startDate,
        LocalDate endDate,
        String aiSummary,
        String suggestions,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long createdBy,
        LocalDateTime createTime
) {}
