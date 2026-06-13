package com.petcare.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO for creating or updating a user address.
 * Coordinate and business rule validation is performed in the application service.
 */
public record AddressUpsertRequest(
        @NotBlank(message = "联系人姓名不能为空")
        @Size(max = 64, message = "联系人姓名最长64个字符")
        String contactName,

        @NotBlank(message = "联系人手机号不能为空")
        @Size(max = 20, message = "联系人手机号最长20个字符")
        String contactPhone,

        @NotBlank(message = "省份不能为空")
        @Size(max = 64, message = "省份最长64个字符")
        String province,

        @NotBlank(message = "城市不能为空")
        @Size(max = 64, message = "城市最长64个字符")
        String city,

        @Size(max = 64, message = "区县最长64个字符")
        String district,

        @NotBlank(message = "详细地址不能为空")
        @Size(max = 255, message = "详细地址最长255个字符")
        String detailAddress,

        BigDecimal longitude,

        BigDecimal latitude,

        Boolean isDefault
) {
}
