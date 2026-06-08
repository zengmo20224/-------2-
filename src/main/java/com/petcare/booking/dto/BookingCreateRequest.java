package com.petcare.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for creating a booking.
 * userId is NOT accepted from the request body — it comes from the security context.
 */
public record BookingCreateRequest(

        @NotNull(message = "门店ID不能为空")
        Long storeId,

        @NotNull(message = "服务项目ID不能为空")
        Long serviceItemId,

        Long petId,

        @NotBlank(message = "服务模式不能为空")
        String serviceMode,

        @NotNull(message = "预约日期不能为空")
        LocalDate bookingDate,

        @NotNull(message = "开始时间不能为空")
        LocalTime startTime,

        Long addressId,

        @NotBlank(message = "联系人姓名不能为空")
        String contactName,

        @NotBlank(message = "联系电话不能为空")
        String contactPhone,

        @NotBlank(message = "付款方式不能为空")
        String paymentMethod,

        String remark
) {
}
