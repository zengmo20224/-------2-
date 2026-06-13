package com.petcare.marketing.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;
import jakarta.validation.constraints.NotBlank;

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
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<String> productNames,
            List<String> serviceNames
    ) {}

    /** Admin list item — includes status and internal fields */
    public record AdminActivitySummary(
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
            String title,
            String activityType,
            String description,
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
            LocalDateTime startTime,
            LocalDateTime endTime,
            String status,
            List<@JsonSerialize(using = SnowflakeIdSerializer.class) Long> productIds,
            List<@JsonSerialize(using = SnowflakeIdSerializer.class) Long> serviceItemIds
    ) {}

    /** Create/update request */
    public record ActivityUpsertRequest(
            @NotBlank String title,
            @NotBlank String activityType,
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
