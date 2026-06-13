package com.petcare.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO for creating or updating a pet profile.
 * Only name and type are required; all other fields are optional.
 * Enum and range validation is performed in the application service.
 */
public record PetUpsertRequest(
        @NotBlank(message = "宠物名称不能为空")
        @Size(max = 64, message = "宠物名称最长64个字符")
        String name,

        @NotNull(message = "宠物类型不能为空")
        String type,

        @Size(max = 64, message = "品种最长64个字符")
        String breed,

        Integer gender,

        BigDecimal age,

        BigDecimal weight,

        String size,

        Integer sterilized,

        @Size(max = 255, message = "头像URL最长255个字符")
        String avatarUrl,

        @Size(max = 500, message = "备注最长500个字符")
        String remark
) {
}
