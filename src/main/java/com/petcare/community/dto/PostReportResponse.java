package com.petcare.community.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.time.LocalDateTime;

/**
 * Response DTO for post reports.
 * Replaces direct PostReport entity exposure with proper ID serialization.
 */
public record PostReportResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long postId,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long reporterId,
        String reasonType,
        String reason,
        String status,
        String handleResult,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long handlerId,
        LocalDateTime handleTime,
        LocalDateTime createTime
) {
}
