package com.petcare.user.service;

import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.user.dto.PetResponse;
import com.petcare.user.dto.PetUpsertRequest;
import com.petcare.user.entity.Pet;
import com.petcare.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Service-layer integration tests for PetApplicationServiceImpl.
 * Directly calls service methods (not through MockMvc/JWT filter)
 * to verify ownership enforcement and validation at the service boundary.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PetApplicationServiceImplTest {

    @Autowired
    private PetApplicationService petApplicationService;

    @Autowired
    private PetService petService;

    @Autowired
    private UserService userService;

    // ======================== Create ========================

    @Nested
    @DisplayName("createCurrentUserPet — validation and ownership")
    class CreatePet {

        @Test
        @DisplayName("creates pet with userId from parameter, not from request")
        void createsPetWithCorrectUserId() {
            User user = createUser("13800138001", "主人", "ACTIVE");

            PetUpsertRequest request = new PetUpsertRequest(
                    "团子", "CAT", null, null, null, null, null, null, null, null);
            PetResponse response = petApplicationService.createCurrentUserPet(user.getId(), request);

            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("团子");

            Pet saved = petService.getById(Long.valueOf(response.petId()));
            assertThat(saved.getUserId()).isEqualTo(user.getId());
        }

        @Test
        @DisplayName("name is trimmed on create")
        void nameIsTrimmed() {
            User user = createUser("13800138002", "主人", "ACTIVE");

            PetUpsertRequest request = new PetUpsertRequest(
                    "  团子  ", "CAT", null, null, null, null, null, null, null, null);
            PetResponse response = petApplicationService.createCurrentUserPet(user.getId(), request);

            assertThat(response.name()).isEqualTo("团子");
        }

        @Test
        @DisplayName("blank optional strings become null")
        void blankOptionalStringsBecomeNull() {
            User user = createUser("13800138003", "主人", "ACTIVE");

            PetUpsertRequest request = new PetUpsertRequest(
                    "团子", "CAT", "  ", null, null, null, "  ", null, "  ", "  ");
            PetResponse response = petApplicationService.createCurrentUserPet(user.getId(), request);

            Pet saved = petService.getById(Long.valueOf(response.petId()));
            assertThat(saved.getBreed()).isNull();
            assertThat(saved.getSize()).isNull();
            assertThat(saved.getAvatarUrl()).isNull();
            assertThat(saved.getRemark()).isNull();
        }

        @Test
        @DisplayName("age with 1 decimal place is accepted")
        void ageWithOneDecimalAccepted() {
            User user = createUser("13800138004", "主人", "ACTIVE");

            PetUpsertRequest request = new PetUpsertRequest(
                    "团子", "CAT", null, null, new BigDecimal("2.5"), null, null, null, null, null);
            PetResponse response = petApplicationService.createCurrentUserPet(user.getId(), request);

            assertThat(response.age()).isEqualByComparingTo(new BigDecimal("2.5"));
        }

        @Test
        @DisplayName("weight with 2 decimal places is accepted")
        void weightWithTwoDecimalsAccepted() {
            User user = createUser("13800138005", "主人", "ACTIVE");

            PetUpsertRequest request = new PetUpsertRequest(
                    "团子", "CAT", null, null, null, new BigDecimal("4.20"), null, null, null, null);
            PetResponse response = petApplicationService.createCurrentUserPet(user.getId(), request);

            assertThat(response.weight()).isEqualByComparingTo(new BigDecimal("4.20"));
        }
    }

    // ======================== List ========================

    @Nested
    @DisplayName("listCurrentUserPets — ownership and sorting")
    class ListPets {

        @Test
        @DisplayName("list only returns pets belonging to the given userId")
        void listOnlyOwnPets() {
            User user1 = createUser("13800138010", "用户A", "ACTIVE");
            User user2 = createUser("13800138011", "用户B", "ACTIVE");

            createPet(user1.getId(), "团子", "CAT");
            createPet(user2.getId(), "旺财", "DOG");
            createPet(user1.getId(), "小黑", "DOG");

            List<PetResponse> pets = petApplicationService.listCurrentUserPets(user1.getId());

            assertThat(pets).hasSize(2)
                    .allSatisfy(p -> assertThat(p.name()).isIn("团子", "小黑"));
        }

        @Test
        @DisplayName("list does not include soft-deleted pets")
        void listExcludesDeleted() {
            User user = createUser("13800138012", "主人", "ACTIVE");
            Pet pet1 = createPet(user.getId(), "团子", "CAT");
            createPet(user.getId(), "旺财", "DOG");

            petService.removeById(pet1.getId());

            List<PetResponse> pets = petApplicationService.listCurrentUserPets(user.getId());
            assertThat(pets).hasSize(1)
                    .first().extracting(PetResponse::name).isEqualTo("旺财");
        }
    }

    // ======================== Detail ========================

    @Nested
    @DisplayName("getCurrentUserPet — ownership enforcement")
    class GetPetDetail {

        @Test
        @DisplayName("returns pet when petId matches userId")
        void returnsPetWhenOwnerMatches() {
            User user = createUser("13800138020", "主人", "ACTIVE");
            Pet pet = createPet(user.getId(), "团子", "CAT");

            PetResponse response = petApplicationService.getCurrentUserPet(user.getId(), pet.getId());

            assertThat(response.petId()).isEqualTo(String.valueOf(pet.getId()));
            assertThat(response.name()).isEqualTo("团子");
        }

        @Test
        @DisplayName("throws resource_not_found when pet belongs to another user")
        void throwsWhenNotOwner() {
            User user1 = createUser("13800138021", "用户A", "ACTIVE");
            User user2 = createUser("13800138022", "用户B", "ACTIVE");
            Pet pet = createPet(user2.getId(), "旺财", "DOG");

            assertThatThrownBy(() -> petApplicationService.getCurrentUserPet(user1.getId(), pet.getId()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        }

        @Test
        @DisplayName("throws resource_not_found for non-existent pet")
        void throwsForNonExistentPet() {
            User user = createUser("13800138023", "主人", "ACTIVE");

            assertThatThrownBy(() -> petApplicationService.getCurrentUserPet(user.getId(), 999999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }

    // ======================== Update ========================

    @Nested
    @DisplayName("updateCurrentUserPet — ownership and field protection")
    class UpdatePet {

        @Test
        @DisplayName("updates allowed fields for own pet")
        void updatesAllowedFields() {
            User user = createUser("13800138030", "主人", "ACTIVE");
            Pet pet = createPet(user.getId(), "团子", "CAT");

            PetUpsertRequest request = new PetUpsertRequest(
                    "大团子", "DOG", "金毛", 1, new BigDecimal("3"), new BigDecimal("15.5"),
                    "LARGE", 0, "https://example.com/new.png", "很活泼");
            PetResponse response = petApplicationService.updateCurrentUserPet(user.getId(), pet.getId(), request);

            assertThat(response.name()).isEqualTo("大团子");
            assertThat(response.type()).isEqualTo("DOG");
            assertThat(response.breed()).isEqualTo("金毛");

            Pet dbPet = petService.getById(pet.getId());
            assertThat(dbPet.getName()).isEqualTo("大团子");
            assertThat(dbPet.getUserId()).isEqualTo(user.getId());
        }

        @Test
        @DisplayName("throws resource_not_found when updating other user's pet")
        void throwsWhenUpdatingOtherUserPet() {
            User user1 = createUser("13800138031", "用户A", "ACTIVE");
            User user2 = createUser("13800138032", "用户B", "ACTIVE");
            Pet pet = createPet(user2.getId(), "旺财", "DOG");

            PetUpsertRequest request = new PetUpsertRequest("被改", "CAT", null, null, null, null, null, null, null, null);

            assertThatThrownBy(() -> petApplicationService.updateCurrentUserPet(user1.getId(), pet.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

            // Verify DB unchanged
            Pet dbPet = petService.getById(pet.getId());
            assertThat(dbPet.getName()).isEqualTo("旺财");
        }

        @Test
        @DisplayName("throws resource_not_found for deleted pet update")
        void throwsForDeletedPetUpdate() {
            User user = createUser("13800138033", "主人", "ACTIVE");
            Pet pet = createPet(user.getId(), "团子", "CAT");
            petService.removeById(pet.getId());

            PetUpsertRequest request = new PetUpsertRequest("更新", "CAT", null, null, null, null, null, null, null, null);

            assertThatThrownBy(() -> petApplicationService.updateCurrentUserPet(user.getId(), pet.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }

    // ======================== Delete ========================

    @Nested
    @DisplayName("deleteCurrentUserPet — ownership enforcement")
    class DeletePet {

        @Test
        @DisplayName("soft-deletes own pet")
        void softDeletesOwnPet() {
            User user = createUser("13800138040", "主人", "ACTIVE");
            Pet pet = createPet(user.getId(), "团子", "CAT");

            petApplicationService.deleteCurrentUserPet(user.getId(), pet.getId());

            assertThat(petService.getById(pet.getId())).isNull();
        }

        @Test
        @DisplayName("throws resource_not_found when deleting other user's pet, DB unchanged")
        void throwsWhenDeletingOtherUserPet() {
            User user1 = createUser("13800138041", "用户A", "ACTIVE");
            User user2 = createUser("13800138042", "用户B", "ACTIVE");
            Pet pet = createPet(user2.getId(), "旺财", "DOG");

            assertThatThrownBy(() -> petApplicationService.deleteCurrentUserPet(user1.getId(), pet.getId()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

            assertThat(petService.getById(pet.getId())).isNotNull();
        }

        @Test
        @DisplayName("throws resource_not_found for already deleted pet")
        void throwsForAlreadyDeletedPet() {
            User user = createUser("13800138043", "主人", "ACTIVE");
            Pet pet = createPet(user.getId(), "团子", "CAT");
            petService.removeById(pet.getId());

            assertThatThrownBy(() -> petApplicationService.deleteCurrentUserPet(user.getId(), pet.getId()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }

    // ======================== Validation ========================

    @Nested
    @DisplayName("Validation — service-layer enum and range checks")
    class Validation {

        @Test
        @DisplayName("invalid type throws validation_error")
        void invalidType() {
            User user = createUser("13800138050", "主人", "ACTIVE");
            PetUpsertRequest request = new PetUpsertRequest("团子", "BIRD", null, null, null, null, null, null, null, null);

            assertThatThrownBy(() -> petApplicationService.createCurrentUserPet(user.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("invalid size throws validation_error")
        void invalidSize() {
            User user = createUser("13800138051", "主人", "ACTIVE");
            PetUpsertRequest request = new PetUpsertRequest("团子", "CAT", null, null, null, null, "HUGE", null, null, null);

            assertThatThrownBy(() -> petApplicationService.createCurrentUserPet(user.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("gender out of range throws validation_error")
        void genderOutOfRange() {
            User user = createUser("13800138052", "主人", "ACTIVE");
            PetUpsertRequest request = new PetUpsertRequest("团子", "CAT", null, 5, null, null, null, null, null, null);

            assertThatThrownBy(() -> petApplicationService.createCurrentUserPet(user.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("sterilized out of range throws validation_error")
        void sterilizedOutOfRange() {
            User user = createUser("13800138053", "主人", "ACTIVE");
            PetUpsertRequest request = new PetUpsertRequest("团子", "CAT", null, null, null, null, null, 3, null, null);

            assertThatThrownBy(() -> petApplicationService.createCurrentUserPet(user.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("age exceeding max throws validation_error")
        void ageExceedingMax() {
            User user = createUser("13800138054", "主人", "ACTIVE");
            PetUpsertRequest request = new PetUpsertRequest("团子", "CAT", null, null, new BigDecimal("1000"), null, null, null, null, null);

            assertThatThrownBy(() -> petApplicationService.createCurrentUserPet(user.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("age with too many decimals throws validation_error")
        void ageTooManyDecimals() {
            User user = createUser("13800138055", "主人", "ACTIVE");
            PetUpsertRequest request = new PetUpsertRequest("团子", "CAT", null, null, new BigDecimal("2.55"), null, null, null, null, null);

            assertThatThrownBy(() -> petApplicationService.createCurrentUserPet(user.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("negative weight throws validation_error")
        void negativeWeight() {
            User user = createUser("13800138056", "主人", "ACTIVE");
            PetUpsertRequest request = new PetUpsertRequest("团子", "CAT", null, null, null, new BigDecimal("-1"), null, null, null, null);

            assertThatThrownBy(() -> petApplicationService.createCurrentUserPet(user.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("invalid avatarUrl protocol throws validation_error")
        void invalidAvatarUrl() {
            User user = createUser("13800138057", "主人", "ACTIVE");
            PetUpsertRequest request = new PetUpsertRequest("团子", "CAT", null, null, null, null, null, null, "ftp://bad.url", null);

            assertThatThrownBy(() -> petApplicationService.createCurrentUserPet(user.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.VALIDATION_ERROR);
        }
    }

    // ======================== Helpers ========================

    private User createUser(String phone, String nickname, String status) {
        User user = new User();
        user.setPhone(phone);
        user.setNickname(nickname);
        user.setStatus(status);
        user.setOpenid("test_openid_" + phone);
        user.setUnionid("test_unionid_" + phone);
        userService.save(user);
        return user;
    }

    private Pet createPet(Long userId, String name, String type) {
        Pet pet = new Pet();
        pet.setUserId(userId);
        pet.setName(name);
        pet.setType(type);
        petService.save(pet);
        return pet;
    }
}
