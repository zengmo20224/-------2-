package com.petcare.user.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.common.security.SecurityContextHelper;
import com.petcare.user.dto.AddressResponse;
import com.petcare.user.dto.AddressUpsertRequest;
import com.petcare.user.service.AddressApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for current user address operations.
 * All endpoints require ROLE_USER explicitly — ADMIN tokens get 403.
 * Current user ID is always derived from SecurityContext, never from request parameters.
 */
@RestController
@RequestMapping("/api/v1/user/addresses")
@PreAuthorize("hasRole('USER')")
public class AddressController {

    private final AddressApplicationService addressApplicationService;

    public AddressController(AddressApplicationService addressApplicationService) {
        this.addressApplicationService = addressApplicationService;
    }

    @GetMapping
    public ApiResponse<List<AddressResponse>> listCurrentUserAddresses() {
        Long currentUserId = requireCurrentUserId();
        List<AddressResponse> addresses = addressApplicationService.listCurrentUserAddresses(currentUserId);
        return ApiResponse.ok(addresses);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> createCurrentUserAddress(
            @Valid @RequestBody AddressUpsertRequest request) {
        Long currentUserId = requireCurrentUserId();
        AddressResponse address = addressApplicationService.createCurrentUserAddress(currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(address));
    }

    @PutMapping("/{addressId}")
    public ApiResponse<AddressResponse> updateCurrentUserAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressUpsertRequest request) {
        Long currentUserId = requireCurrentUserId();
        AddressResponse address = addressApplicationService.updateCurrentUserAddress(currentUserId, addressId, request);
        return ApiResponse.ok(address);
    }

    @DeleteMapping("/{addressId}")
    public ApiResponse<Void> deleteCurrentUserAddress(@PathVariable Long addressId) {
        Long currentUserId = requireCurrentUserId();
        addressApplicationService.deleteCurrentUserAddress(currentUserId, addressId);
        return ApiResponse.ok(null);
    }

    private Long requireCurrentUserId() {
        return SecurityContextHelper.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录"));
    }
}
