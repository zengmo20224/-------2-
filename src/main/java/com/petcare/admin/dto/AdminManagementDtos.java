package com.petcare.admin.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class AdminManagementDtos {

    private AdminManagementDtos() {
    }

    public record StoreUpdateRequest(
            @NotBlank @Size(max = 100) String storeName,
            @Size(max = 20) String phone,
            @Size(max = 255) String address,
            BigDecimal longitude,
            BigDecimal latitude,
            @Size(max = 100) String businessHours,
            @NotBlank @Pattern(regexp = "OPEN|CLOSED") String status,
            @Size(max = 500) String description) {
    }

    public record StoreView(
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
            String storeName, String phone, String address,
            BigDecimal longitude, BigDecimal latitude, String businessHours,
            String status, String description) {
    }

    public record StoreConfigUpdateRequest(
            @NotNull @DecimalMin("0.1") BigDecimal homeServiceRadiusKm,
            @NotNull @Min(1) @Max(365) Integer bookingAdvanceDays,
            @NotNull @Min(0) @Max(168) Integer bookingCancelHours,
            @NotNull @Min(5) @Max(240) Integer timeSlotMinutes,
            @NotNull Boolean autoConfirmBooking,
            @NotNull Boolean contentAutoPublish) {
    }

    public record StoreConfigView(
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long storeId,
            BigDecimal homeServiceRadiusKm,
            Integer bookingAdvanceDays, Integer bookingCancelHours,
            Integer timeSlotMinutes, Boolean autoConfirmBooking,
            Boolean contentAutoPublish) {
    }

    public record ServiceItemRequest(
            @NotNull @Positive Long categoryId,
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Pattern(regexp = "STORE|HOME|BOTH") String serviceMode,
            @NotNull @DecimalMin("0.0") BigDecimal price,
            @NotNull @Positive Integer durationMinutes,
            @Pattern(regexp = "DOG|CAT|ALL") String petType,
            @Pattern(regexp = "SMALL|MEDIUM|LARGE|ALL") String petSize,
            @NotNull Boolean needAddress,
            @NotNull Boolean needPet,
            String description,
            @Size(max = 255) String coverUrl,
            @Min(0) Integer sort) {
    }

    public record ServiceItemView(
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long categoryId,
            String name, String serviceMode,
            BigDecimal price, Integer durationMinutes, String petType,
            String petSize, Boolean needAddress, Boolean needPet,
            String description, String coverUrl, String status, Integer sort) {
    }

    public record StaffRequest(
            @NotNull @Positive Long storeId,
            @NotBlank @Size(max = 64) String name,
            @Size(max = 20) String phone,
            @Size(max = 255) String avatarUrl,
            @NotBlank @Pattern(regexp = "GROOMER|WALKER|FEEDER|MANAGER") String role,
            @Size(max = 500) String description) {
    }

    public record StaffView(
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long storeId,
            String name, String phone, String avatarUrl,
            String role, String status, String description) {
    }

    public record StaffSkillUpdateRequest(@NotNull List<@NotNull @Positive Long> serviceCategoryIds) {
    }

    public record StaffSkillView(
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long staffId,
            @JsonSerialize(contentUsing = SnowflakeIdSerializer.class) List<Long> serviceCategoryIds) {
    }

    public record StaffScheduleRequest(
            @NotNull @Positive Long storeId,
            @NotNull LocalDate workDate,
            @NotNull LocalTime startTime,
            @NotNull LocalTime endTime,
            @NotBlank @Pattern(regexp = "AVAILABLE|UNAVAILABLE") String status,
            @Size(max = 255) String remark) {
    }

    public record StaffScheduleView(
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long staffId,
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long storeId,
            LocalDate workDate,
            LocalTime startTime, LocalTime endTime, String status, String remark) {
    }

    public record ProductRequest(
            @NotNull @Positive Long categoryId,
            @NotBlank @Size(max = 100) String name,
            @Size(max = 255) String coverUrl,
            @NotNull @DecimalMin("0.0") BigDecimal price,
            String description,
            @NotNull Boolean pickupOnly,
            @Min(0) Integer sort) {
    }

    public record ProductStockUpdateRequest(@NotNull @Min(0) Integer stock) {
    }

    public record ProductView(
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long categoryId,
            String name, String coverUrl,
            BigDecimal price, Integer stock, Integer salesCount,
            String description, Boolean pickupOnly, String status, Integer sort) {
    }

    public record OperationLogView(
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
            @JsonSerialize(using = SnowflakeIdSerializer.class) Long adminId,
            String module, String operation,
            String requestMethod, String requestUrl, String result,
            String errorMessage, LocalDateTime createTime) {
    }
}
