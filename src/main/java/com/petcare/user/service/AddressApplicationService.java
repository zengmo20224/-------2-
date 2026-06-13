package com.petcare.user.service;

import com.petcare.user.dto.AddressResponse;
import com.petcare.user.dto.AddressUpsertRequest;

import java.util.List;

/**
 * Application service for current user address CRUD.
 * All methods require the current user's ID from the security context.
 * Ownership is always enforced by the userId + addressId condition.
 */
public interface AddressApplicationService {

    List<AddressResponse> listCurrentUserAddresses(Long currentUserId);

    AddressResponse createCurrentUserAddress(Long currentUserId, AddressUpsertRequest request);

    AddressResponse updateCurrentUserAddress(Long currentUserId, Long addressId, AddressUpsertRequest request);

    void deleteCurrentUserAddress(Long currentUserId, Long addressId);
}
