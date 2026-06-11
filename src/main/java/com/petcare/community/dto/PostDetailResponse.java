package com.petcare.community.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for post detail view.
 * Includes comments preview.
 */
public record PostDetailResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long userId,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long petId,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long topicId,
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
