package com.petcare.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for reporting a post.
 */
public record ReportPostRequest(
        @NotBlank(message = "举报类型不能为空")
        String reasonType,
        @Size(max = 500, message = "举报原因不能超过500字")
        String reason
) {
}
