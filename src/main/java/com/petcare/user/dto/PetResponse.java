package com.petcare.user.dto;

import com.petcare.user.entity.Pet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for pet profile.
 * Exposes safe fields only; userId and deleted are never returned.
 * petId is always serialized as String to avoid JavaScript precision loss.
 */
public record PetResponse(
        String petId,
        String name,
        String type,
        String breed,
        Integer gender,
        BigDecimal age,
        BigDecimal weight,
        String size,
        Integer sterilized,
        String avatarUrl,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Creates a PetResponse from a Pet entity.
     * userId and deleted are intentionally excluded.
     */
    public static PetResponse from(Pet pet) {
        return new PetResponse(
                String.valueOf(pet.getId()),
                pet.getName(),
                pet.getType(),
                pet.getBreed(),
                pet.getGender(),
                pet.getAge(),
                pet.getWeight(),
                pet.getSize(),
                pet.getSterilized(),
                pet.getAvatarUrl(),
                pet.getRemark(),
                pet.getCreateTime(),
                pet.getUpdateTime()
        );
    }
}
