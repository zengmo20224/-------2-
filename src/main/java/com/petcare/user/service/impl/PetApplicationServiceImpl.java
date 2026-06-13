package com.petcare.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.user.dto.PetResponse;
import com.petcare.user.dto.PetUpsertRequest;
import com.petcare.user.entity.Pet;
import com.petcare.user.service.PetApplicationService;
import com.petcare.user.service.PetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Application service for current user pet profile CRUD.
 * Operates through PetService (not PetMapper directly).
 * Enforces ownership on all read, update, and delete operations.
 */
@Service
public class PetApplicationServiceImpl implements PetApplicationService {

    private static final Set<String> VALID_TYPES = Set.of("DOG", "CAT", "OTHER");
    private static final Set<String> VALID_SIZES = Set.of("SMALL", "MEDIUM", "LARGE");
    private static final BigDecimal MAX_AGE = new BigDecimal("999.9");
    private static final BigDecimal MAX_WEIGHT = new BigDecimal("999.99");

    private final PetService petService;

    public PetApplicationServiceImpl(PetService petService) {
        this.petService = petService;
    }

    @Override
    public List<PetResponse> listCurrentUserPets(Long currentUserId) {
        List<Pet> pets = petService.list(new LambdaQueryWrapper<Pet>()
                .eq(Pet::getUserId, currentUserId)
                .orderByDesc(Pet::getCreateTime)
                .orderByDesc(Pet::getId));
        return pets.stream().map(PetResponse::from).toList();
    }

    @Override
    public PetResponse getCurrentUserPet(Long currentUserId, Long petId) {
        Pet pet = findUserPet(currentUserId, petId);
        return PetResponse.from(pet);
    }

    @Override
    @Transactional
    public PetResponse createCurrentUserPet(Long currentUserId, PetUpsertRequest request) {
        validatePetRequest(request);

        Pet pet = new Pet();
        pet.setUserId(currentUserId);
        setPetFields(pet, request);

        petService.save(pet);
        return PetResponse.from(pet);
    }

    @Override
    @Transactional
    public PetResponse updateCurrentUserPet(Long currentUserId, Long petId, PetUpsertRequest request) {
        validatePetRequest(request);

        // Verify existence and ownership before updating
        findUserPet(currentUserId, petId);

        LambdaUpdateWrapper<Pet> wrapper = new LambdaUpdateWrapper<Pet>()
                .eq(Pet::getId, petId)
                .eq(Pet::getUserId, currentUserId)
                .set(Pet::getName, request.name().trim())
                .set(Pet::getType, request.type())
                .set(Pet::getBreed, normalizeBlank(request.breed()))
                .set(Pet::getGender, request.gender())
                .set(Pet::getAge, request.age())
                .set(Pet::getWeight, request.weight())
                .set(Pet::getSize, normalizeBlank(request.size()))
                .set(Pet::getSterilized, request.sterilized())
                .set(Pet::getAvatarUrl, validateAndNormalizeAvatarUrl(request.avatarUrl()))
                .set(Pet::getRemark, normalizeBlank(request.remark()));

        if (!petService.update(wrapper)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "宠物不存在");
        }

        return PetResponse.from(findUserPet(currentUserId, petId));
    }

    @Override
    @Transactional
    public void deleteCurrentUserPet(Long currentUserId, Long petId) {
        boolean removed = petService.remove(new LambdaQueryWrapper<Pet>()
                .eq(Pet::getId, petId)
                .eq(Pet::getUserId, currentUserId));
        if (!removed) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "宠物不存在");
        }
    }

    // ======================== Private helpers ========================

    private Pet findUserPet(Long currentUserId, Long petId) {
        Pet pet = petService.getOne(new LambdaQueryWrapper<Pet>()
                .eq(Pet::getId, petId)
                .eq(Pet::getUserId, currentUserId));
        if (pet == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "宠物不存在");
        }
        return pet;
    }

    private void setPetFields(Pet pet, PetUpsertRequest request) {
        pet.setName(request.name().trim());
        pet.setType(request.type());
        pet.setBreed(normalizeBlank(request.breed()));
        pet.setGender(request.gender());
        pet.setAge(request.age());
        pet.setWeight(request.weight());
        pet.setSize(normalizeBlank(request.size()));
        pet.setSterilized(request.sterilized());
        pet.setAvatarUrl(validateAndNormalizeAvatarUrl(request.avatarUrl()));
        pet.setRemark(normalizeBlank(request.remark()));
    }

    private void validatePetRequest(PetUpsertRequest request) {
        if (!VALID_TYPES.contains(request.type())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "宠物类型必须是 DOG、CAT 或 OTHER");
        }

        String size = normalizeBlank(request.size());
        if (size != null && !VALID_SIZES.contains(size)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "体型必须是 SMALL、MEDIUM 或 LARGE");
        }

        if (request.gender() != null && (request.gender() < 0 || request.gender() > 2)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "性别值必须是 0、1 或 2");
        }

        if (request.sterilized() != null && (request.sterilized() < 0 || request.sterilized() > 1)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "绝育状态必须是 0 或 1");
        }

        if (request.age() != null) {
            if (request.age().stripTrailingZeros().scale() > 1) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "年龄最多1位小数");
            }
            if (request.age().compareTo(BigDecimal.ZERO) < 0 || request.age().compareTo(MAX_AGE) > 0) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "年龄范围为 0 到 999.9");
            }
        }

        if (request.weight() != null) {
            if (request.weight().stripTrailingZeros().scale() > 2) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "体重最多2位小数");
            }
            if (request.weight().compareTo(BigDecimal.ZERO) < 0 || request.weight().compareTo(MAX_WEIGHT) > 0) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "体重范围为 0 到 999.99");
            }
        }
    }

    private String validateAndNormalizeAvatarUrl(String avatarUrl) {
        String normalized = normalizeBlank(avatarUrl);
        if (normalized != null) {
            String lower = normalized.toLowerCase();
            if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "头像URL只允许http或https协议");
            }
        }
        return normalized;
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
