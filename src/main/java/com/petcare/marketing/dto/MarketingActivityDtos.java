package com.petcare.marketing.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs for marketing activity endpoints.
 * Public and admin DTOs share this file to keep the module compact.
 */
public final class MarketingActivityDtos {

    private MarketingActivityDtos() {}

    /** Public summary — no internal status, no timestamps beyond display range */
    public record PublicActivitySummary(
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
            String title,
            String activityType,
            String description,
            String coverUrl,
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<ActivityProductCard> products,
            List<ActivityServiceCard> services,
            List<String> productNames,
            List<String> serviceNames
    ) {}

    /** Product card shown inside public activity pages */
    public record ActivityProductCard(
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
            String name,
            String coverUrl,
            BigDecimal price,
            Integer salesCount
    ) {}

    /** Service card shown inside public activity pages */
    public record ActivityServiceCard(
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
            String name,
            String coverUrl,
            BigDecimal price,
            Integer durationMinutes,
            String serviceMode
    ) {}

    /** Admin list item — includes status and internal fields */
    public record AdminActivitySummary(
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
            String title,
            String activityType,
            String description,
            String coverUrl,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String status
    ) {}

    /** Admin detail — includes associated product/service IDs */
    public record AdminActivityDetail(
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
            String title,
            String activityType,
            String description,
            String coverUrl,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String status,
            @JsonSerialize(contentUsing = SnowflakeIdSerializer.class) List<Long> productIds,
            @JsonSerialize(contentUsing = SnowflakeIdSerializer.class) List<Long> serviceItemIds
    ) {}

    /** Create/update request */
    public record ActivityUpsertRequest(
            @NotBlank String title,
            @NotBlank String activityType,
            @Size(max = 255) String coverUrl,
            String description,
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<Long> productIds,
            List<Long> serviceItemIds
    ) {}

    /** Status change request (activate/end/cancel) */
    public record ActivityStatusRequest(
            @NotBlank String status
    ) {}
}
