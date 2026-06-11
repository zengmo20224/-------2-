package com.petcare.community.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.time.LocalDateTime;

/**
 * Response DTO for comment list items.
 * Does not expose internal fields: riskLevel.
 */
public record CommentResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long postId,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long userId,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long parentId,
        String content,
        String status,
        Integer likeCount,
        LocalDateTime createTime
) {
}
