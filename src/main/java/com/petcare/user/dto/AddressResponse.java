package com.petcare.user.dto;

import com.petcare.user.entity.UserAddress;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for user address.
 * Exposes safe fields only; userId and deleted are never returned.
 * addressId is always serialized as String to avoid JavaScript precision loss.
 */
public record AddressResponse(
        String addressId,
        String contactName,
        String contactPhone,
        String province,
        String city,
        String district,
        String detailAddress,
        BigDecimal longitude,
        BigDecimal latitude,
        Boolean isDefault,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Creates an AddressResponse from a UserAddress entity.
     * userId and deleted are intentionally excluded.
     */
    public static AddressResponse from(UserAddress addr) {
        return new AddressResponse(
                String.valueOf(addr.getId()),
                addr.getContactName(),
                addr.getContactPhone(),
                addr.getProvince(),
                addr.getCity(),
                addr.getDistrict(),
                addr.getDetailAddress(),
                addr.getLongitude(),
                addr.getLatitude(),
                addr.getIsDefault() != null && addr.getIsDefault() == 1,
                addr.getCreateTime(),
                addr.getUpdateTime()
        );
    }
}
