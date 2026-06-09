package com.petcare.ai.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * Request DTO for creating an AI analysis report.
 */
public record AiAnalysisCreateRequest(

        @NotNull(message = "报告类型不能为空")
        @Pattern(regexp = "BUSINESS|COMMUNITY|SALES|ACTIVITY", message = "报告类型不合法")
        String reportType,

        @NotNull(message = "开始日期不能为空")
        LocalDate startDate,

        @NotNull(message = "结束日期不能为空")
        LocalDate endDate
) {}
