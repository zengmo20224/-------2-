package com.petcare.community.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Public-facing comment in tree structure for anonymous readers.
 * Top-level comments include their replies as a nested list.
 */
public record PublicCommentTreeResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long parentId,
        String content,
        Integer likeCount,
        LocalDateTime createTime,
        String authorName,
        String authorAvatar,
        List<PublicCommentTreeResponse> replies
) {
}
