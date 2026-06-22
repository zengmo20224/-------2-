package com.petcare.notification.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.time.LocalDateTime;

/**
 * Admin-facing announcement response (includes status and deleted fields).
 */
public record AdminAnnouncementResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        String title,
        String content,
        String status,
        Integer sort,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}
