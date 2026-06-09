package com.petcare.community.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for comment list items.
 * Does not expose internal fields: riskLevel.
 */
public record CommentResponse(
        Long id,
        Long postId,
        Long userId,
        Long parentId,
        String content,
        String status,
        Integer likeCount,
        LocalDateTime createTime
) {
}
