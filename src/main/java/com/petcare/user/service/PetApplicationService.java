package com.petcare.user.service;

import com.petcare.user.dto.PetResponse;
import com.petcare.user.dto.PetUpsertRequest;

import java.util.List;

/**
 * Application service for current user pet profile operations.
 * All methods require the current user's ID from the security context.
 * Ownership is always enforced by the userId + petId condition.
 */
public interface PetApplicationService {

    List<PetResponse> listCurrentUserPets(Long currentUserId);

    PetResponse getCurrentUserPet(Long currentUserId, Long petId);

    PetResponse createCurrentUserPet(Long currentUserId, PetUpsertRequest request);

    PetResponse updateCurrentUserPet(Long currentUserId, Long petId, PetUpsertRequest request);

    void deleteCurrentUserPet(Long currentUserId, Long petId);
}
