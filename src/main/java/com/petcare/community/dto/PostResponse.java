package com.petcare.community.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for post list items.
 * Does not expose internal fields: riskLevel, rejectReason.
 */
public record PostResponse(
        Long id,
        Long userId,
        Long petId,
        Long topicId,
        String title,
        String content,
        String status,
        Integer viewCount,
        Integer likeCount,
        Integer commentCount,
        Integer favoriteCount,
        LocalDateTime publishTime,
        LocalDateTime createTime
) {
}
