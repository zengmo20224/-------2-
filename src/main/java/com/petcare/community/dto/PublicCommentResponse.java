package com.petcare.community.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.time.LocalDateTime;

/**
 * Public-facing comment for anonymous readers.
 *
 * <p>Strips private identifiers and internal moderation fields:
 * no userId, postId (caller already knows the post), status, riskLevel
 * or deleted markers.
 */
public record PublicCommentResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long parentId,
        String content,
        Integer likeCount,
        LocalDateTime createTime
) {
}
