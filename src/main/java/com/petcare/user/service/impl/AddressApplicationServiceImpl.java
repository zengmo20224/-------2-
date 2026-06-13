package com.petcare.user.service.impl;

import com.petcare.user.dto.AddressResponse;
import com.petcare.user.dto.AddressUpsertRequest;
import com.petcare.user.service.AddressApplicationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service for current user address CRUD.
 * RED phase: stubs that throw to make tests fail.
 */
@Service
public class AddressApplicationServiceImpl implements AddressApplicationService {

    @Override
    public List<AddressResponse> listCurrentUserAddresses(Long currentUserId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public AddressResponse createCurrentUserAddress(Long currentUserId, AddressUpsertRequest request) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public AddressResponse updateCurrentUserAddress(Long currentUserId, Long addressId, AddressUpsertRequest request) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void deleteCurrentUserAddress(Long currentUserId, Long addressId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
