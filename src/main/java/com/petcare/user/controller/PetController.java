package com.petcare.user.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.common.security.SecurityContextHelper;
import com.petcare.user.dto.PetResponse;
import com.petcare.user.dto.PetUpsertRequest;
import com.petcare.user.service.PetApplicationService;
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
 * REST controller for current user pet profile operations.
 * All endpoints require ROLE_USER explicitly — ADMIN tokens get 403.
 * Current user ID is always derived from SecurityContext, never from request parameters.
 */
@RestController
@RequestMapping("/api/v1/user/pets")
@PreAuthorize("hasRole('USER')")
public class PetController {

    private final PetApplicationService petApplicationService;

    public PetController(PetApplicationService petApplicationService) {
        this.petApplicationService = petApplicationService;
    }

    @GetMapping
    public ApiResponse<List<PetResponse>> listCurrentUserPets() {
        Long currentUserId = requireCurrentUserId();
        List<PetResponse> pets = petApplicationService.listCurrentUserPets(currentUserId);
        return ApiResponse.ok(pets);
    }

    @GetMapping("/{petId}")
    public ApiResponse<PetResponse> getCurrentUserPet(@PathVariable Long petId) {
        Long currentUserId = requireCurrentUserId();
        PetResponse pet = petApplicationService.getCurrentUserPet(currentUserId, petId);
        return ApiResponse.ok(pet);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PetResponse>> createCurrentUserPet(
            @Valid @RequestBody PetUpsertRequest request) {
        Long currentUserId = requireCurrentUserId();
        PetResponse pet = petApplicationService.createCurrentUserPet(currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(pet));
    }

    @PutMapping("/{petId}")
    public ApiResponse<PetResponse> updateCurrentUserPet(
            @PathVariable Long petId,
            @Valid @RequestBody PetUpsertRequest request) {
        Long currentUserId = requireCurrentUserId();
        PetResponse pet = petApplicationService.updateCurrentUserPet(currentUserId, petId, request);
        return ApiResponse.ok(pet);
    }

    @DeleteMapping("/{petId}")
    public ApiResponse<Void> deleteCurrentUserPet(@PathVariable Long petId) {
        Long currentUserId = requireCurrentUserId();
        petApplicationService.deleteCurrentUserPet(currentUserId, petId);
        return ApiResponse.ok(null);
    }

    private Long requireCurrentUserId() {
        return SecurityContextHelper.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录"));
    }
}
