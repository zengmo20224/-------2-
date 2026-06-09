package com.petcare.community.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for admin reviewing a post or comment.
 */
public record AdminReviewRequest(
        @Size(max = 500, message = "审核备注不能超过500字")
        String remark
) {
}
