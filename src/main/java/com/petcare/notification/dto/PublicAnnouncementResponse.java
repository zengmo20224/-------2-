package com.petcare.notification.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.time.LocalDateTime;

/**
 * Public announcement response for anonymous readers.
 */
public record PublicAnnouncementResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        String title,
        String content,
        Integer sort,
        LocalDateTime createTime
) {
}
