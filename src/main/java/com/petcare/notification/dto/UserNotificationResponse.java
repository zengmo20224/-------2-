package com.petcare.notification.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.time.LocalDateTime;

/**
 * User-facing notification item.
 */
public record UserNotificationResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        String type,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long postId,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long commentId,
        String content,
        Boolean isRead,
        String actorName,
        String actorAvatar,
        LocalDateTime createTime
) {
}
