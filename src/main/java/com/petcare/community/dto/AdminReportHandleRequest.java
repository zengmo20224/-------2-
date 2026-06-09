package com.petcare.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for admin handling a report.
 */
public record AdminReportHandleRequest(
        @NotBlank(message = "处理结果不能为空")
        String handleResult,
        boolean hidePost,
        @Size(max = 500, message = "处理说明不能超过500字")
        String handleRemark
) {
}
