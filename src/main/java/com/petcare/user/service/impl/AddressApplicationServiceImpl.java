package com.petcare.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.user.dto.AddressResponse;
import com.petcare.user.dto.AddressUpsertRequest;
import com.petcare.user.entity.UserAddress;
import com.petcare.user.service.AddressApplicationService;
import com.petcare.user.service.UserAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Application service for current user address CRUD.
 * Operates through UserAddressService (not mapper directly).
 * Enforces ownership on all read, update, and delete operations.
 */
@Service
public class AddressApplicationServiceImpl implements AddressApplicationService {

    private static final BigDecimal LNG_MIN = new BigDecimal("-180");
    private static final BigDecimal LNG_MAX = new BigDecimal("180");
    private static final BigDecimal LAT_MIN = new BigDecimal("-90");
    private static final BigDecimal LAT_MAX = new BigDecimal("90");
    private static final int MAX_COORDINATE_SCALE = 6;

    private final UserAddressService addressService;

    public AddressApplicationServiceImpl(UserAddressService addressService) {
        this.addressService = addressService;
    }

    @Override
    public List<AddressResponse> listCurrentUserAddresses(Long currentUserId) {
        List<UserAddress> addresses = addressService.list(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, currentUserId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getCreateTime)
                .orderByDesc(UserAddress::getId));
        return addresses.stream().map(AddressResponse::from).toList();
    }

    @Override
    @Transactional
    public AddressResponse createCurrentUserAddress(Long currentUserId, AddressUpsertRequest request) {
        validateRequest(request);

        UserAddress address = new UserAddress();
        address.setUserId(currentUserId);
        setAddressFields(address, request);

        // First address auto-becomes default
        long existingCount = addressService.count(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, currentUserId));
        if (existingCount == 0) {
            address.setIsDefault(1);
        } else if (request.isDefault() != null && request.isDefault()) {
            unsetOtherDefaults(currentUserId, null);
            address.setIsDefault(1);
        } else {
            address.setIsDefault(0);
        }

        if (!addressService.save(address)) {
            throw new IllegalStateException("地址保存失败");
        }
        return AddressResponse.from(address);
    }

    @Override
    @Transactional
    public AddressResponse updateCurrentUserAddress(Long currentUserId, Long addressId, AddressUpsertRequest request) {
        // Full implementation in GREEN-2 with default address logic
        throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "地址不存在");
    }

    @Override
    @Transactional
    public void deleteCurrentUserAddress(Long currentUserId, Long addressId) {
        // Full implementation in GREEN-2 with default address logic
        throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "地址不存在");
    }

    // ======================== Private helpers ========================

    private void validateRequest(AddressUpsertRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "请求不能为空");
        }

        // Required field checks at service layer
        if (request.contactName() == null || request.contactName().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "联系人姓名不能为空");
        }
        if (request.contactPhone() == null || request.contactPhone().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "联系人手机号不能为空");
        }
        if (request.province() == null || request.province().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "省份不能为空");
        }
        if (request.city() == null || request.city().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "城市不能为空");
        }
        if (request.detailAddress() == null || request.detailAddress().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "详细地址不能为空");
        }

        // Coordinate pair validation: both or neither
        boolean hasLng = request.longitude() != null;
        boolean hasLat = request.latitude() != null;
        if (hasLng != hasLat) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "经纬度必须同时存在或同时为空");
        }

        if (hasLng) {
            validateCoordinate(request.longitude(), LNG_MIN, LNG_MAX, "经度");
            validateCoordinate(request.latitude(), LAT_MIN, LAT_MAX, "纬度");
        }
    }

    private void validateCoordinate(BigDecimal value, BigDecimal min, BigDecimal max, String name) {
        if (value.stripTrailingZeros().scale() > MAX_COORDINATE_SCALE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    name + "最多" + MAX_COORDINATE_SCALE + "位小数");
        }
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    name + "范围为 " + min + " 到 " + max);
        }
    }

    private void setAddressFields(UserAddress address, AddressUpsertRequest request) {
        address.setContactName(request.contactName().trim());
        address.setContactPhone(request.contactPhone().trim());
        address.setProvince(request.province().trim());
        address.setCity(request.city().trim());
        address.setDetailAddress(request.detailAddress().trim());
        address.setDistrict(normalizeBlank(request.district()));
        address.setLongitude(request.longitude());
        address.setLatitude(request.latitude());
    }

    private void unsetOtherDefaults(Long currentUserId, Long excludeAddressId) {
        LambdaUpdateWrapper<UserAddress> wrapper = new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, currentUserId)
                .eq(UserAddress::getIsDefault, 1)
                .set(UserAddress::getIsDefault, 0);
        if (excludeAddressId != null) {
            wrapper.ne(UserAddress::getId, excludeAddressId);
        }
        addressService.update(wrapper);
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
