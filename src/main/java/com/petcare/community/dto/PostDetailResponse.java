package com.petcare.community.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for post detail view.
 * Includes comments preview.
 */
public record PostDetailResponse(
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
        LocalDateTime createTime,
        List<String> imageUrls
) {
}
