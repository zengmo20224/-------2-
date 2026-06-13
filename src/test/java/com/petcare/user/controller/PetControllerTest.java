package com.petcare.user.controller;

import com.petcare.admin.entity.AdminUser;
import com.petcare.admin.service.AdminUserService;
import com.petcare.common.security.JwtTokenService;
import com.petcare.user.entity.Pet;
import com.petcare.user.entity.User;
import com.petcare.user.service.PetService;
import com.petcare.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RED-1 / RED-2: Integration tests for current user pet profile API.
 * Contract defined in docs/37-phase-11-03-glm5-pet-profile-api-brief.md.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    @SpyBean
    private PetService petService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ======================== RED-1: Create Contract ========================

    @Nested
    @DisplayName("POST /api/v1/user/pets — create")
    class CreatePet {

        @Test
        @DisplayName("USER creates pet, petId is String, no userId/deleted in response")
        void userCreatesPetSuccessfully() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validPetJson("团子", "CAT")))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.petId").isString())
                    .andExpect(jsonPath("$.data.name").value("团子"))
                    .andExpect(jsonPath("$.data.type").value("CAT"))
                    .andExpect(jsonPath("$.data.userId").doesNotExist())
                    .andExpect(jsonPath("$.data.deleted").doesNotExist());
        }

        @Test
        @DisplayName("DB userId from token, forged userId in request ignored")
        void userIdFromTokenNotFromRequest() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"团子\",\"type\":\"CAT\",\"userId\":999999}"))
                    .andExpect(status().isCreated());

            assertThat(petService.list()).hasSize(1);
            Pet saved = petService.list().get(0);
            assertThat(saved.getUserId()).isEqualTo(user.getId());
        }

        @Test
        @DisplayName("name is trimmed before save")
        void nameIsTrimmed() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"  团子  \",\"type\":\"DOG\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.name").value("团子"));
        }

        @Test
        @DisplayName("null name returns 400 validation_error")
        void nullNameReturns400() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"type\":\"CAT\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("blank name returns 400 validation_error")
        void blankNameReturns400() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"   \",\"type\":\"CAT\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("null type returns 400 validation_error")
        void nullTypeReturns400() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"团子\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("invalid type returns 400")
        void invalidTypeReturns400() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"团子\",\"type\":\"BIRD\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("invalid size returns 400")
        void invalidSizeReturns400() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"团子\",\"type\":\"CAT\",\"size\":\"HUGE\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("negative age returns 400")
        void negativeAgeReturns400() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"团子\",\"type\":\"CAT\",\"age\":-1}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("age exceeding 999.9 returns 400")
        void ageExceedingMaxReturns400() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"团子\",\"type\":\"CAT\",\"age\":1000}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("age with more than 1 decimal place returns 400")
        void ageWithTooManyDecimalsReturns400() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"团子\",\"type\":\"CAT\",\"age\":2.55}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("invalid avatarUrl protocol returns 400")
        void invalidAvatarUrlReturns400() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"团子\",\"type\":\"CAT\",\"avatarUrl\":\"ftp://example.com/pet.png\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("blank optional strings become null in database")
        void blankOptionalStringsBecomeNull() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"团子\",\"type\":\"CAT\",\"breed\":\"  \",\"avatarUrl\":\"  \",\"remark\":\"  \"}"))
                    .andExpect(status().isCreated());

            Pet saved = petService.list().get(0);
            assertThat(saved.getBreed()).isNull();
            assertThat(saved.getAvatarUrl()).isNull();
            assertThat(saved.getRemark()).isNull();
        }

        @Test
        @DisplayName("gender out of range returns 400")
        void genderOutOfRangeReturns400() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"团子\",\"type\":\"CAT\",\"gender\":5}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("sterilized out of range returns 400")
        void sterilizedOutOfRangeReturns400() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"团子\",\"type\":\"CAT\",\"sterilized\":3}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }
    }

    // ======================== 11-03R LOW: internal_error 500 ========================

    @Nested
    @DisplayName("POST /api/v1/user/pets — internal error")
    class InternalError {

        @Test
        @DisplayName("save failure returns 500 internal_error")
        void saveFailureReturns500() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            // Stub save to return false, triggering IllegalStateException
            doReturn(false).when(petService).save(any());

            mockMvc.perform(post("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validPetJson("团子", "CAT")))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error.code").value("internal_error"));

            // Verify no pet was persisted
            assertThat(petService.list()).isEmpty();
        }
    }

    // ======================== RED-1: List Contract ========================

    @Nested
    @DisplayName("GET /api/v1/user/pets — list")
    class ListPets {

        @Test
        @DisplayName("list only returns current user's non-deleted pets")
        void listOnlyOwnNonDeletedPets() throws Exception {
            User user1 = createUser("13800138001", "用户A", "ACTIVE");
            User user2 = createUser("13800138002", "用户B", "ACTIVE");

            createPet(user1.getId(), "团子", "CAT");
            createPet(user2.getId(), "旺财", "DOG");
            createPet(user1.getId(), "小黑", "DOG");

            String token1 = jwtTokenService.signUserToken(user1.getId());

            mockMvc.perform(get("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[?(@.name=='团子')]").exists())
                    .andExpect(jsonPath("$.data[?(@.name=='小黑')]").exists())
                    .andExpect(jsonPath("$.data[?(@.name=='旺财')]").doesNotExist());
        }

        @Test
        @DisplayName("list excludes soft-deleted pets")
        void listExcludesDeletedPets() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            Pet pet1 = createPet(user.getId(), "团子", "CAT");
            createPet(user.getId(), "旺财", "DOG");

            petService.removeById(pet1.getId());

            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(get("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].name").value("旺财"));
        }

        @Test
        @DisplayName("list sorted by createTime DESC then id DESC")
        void listSortedDesc() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            createPet(user.getId(), "第一只", "CAT");
            createPet(user.getId(), "第二只", "DOG");
            createPet(user.getId(), "第三只", "OTHER");

            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(get("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].name").value("第三只"))
                    .andExpect(jsonPath("$.data[1].name").value("第二只"))
                    .andExpect(jsonPath("$.data[2].name").value("第一只"));
        }

        @Test
        @DisplayName("empty list when user has no pets")
        void emptyListWhenNoPets() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(get("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("list items: petId is String, no userId/deleted")
        void listItemShape() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            createPet(user.getId(), "团子", "CAT");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(get("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].petId").isString())
                    .andExpect(jsonPath("$.data[0].userId").doesNotExist())
                    .andExpect(jsonPath("$.data[0].deleted").doesNotExist());
        }
    }

    // ======================== RED-2: Detail Contract ========================

    @Nested
    @DisplayName("GET /api/v1/user/pets/{petId} — detail")
    class GetPetDetail {

        @Test
        @DisplayName("user views own pet detail")
        void userViewsOwnPetDetail() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            Pet pet = createPet(user.getId(), "团子", "CAT");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(get("/api/v1/user/pets/" + pet.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.petId").value(String.valueOf(pet.getId())))
                    .andExpect(jsonPath("$.data.name").value("团子"))
                    .andExpect(jsonPath("$.data.type").value("CAT"))
                    .andExpect(jsonPath("$.data.userId").doesNotExist())
                    .andExpect(jsonPath("$.data.deleted").doesNotExist());
        }

        @Test
        @DisplayName("user cannot view other user's pet — 404")
        void userCannotViewOtherUserPet() throws Exception {
            User user1 = createUser("13800138001", "用户A", "ACTIVE");
            User user2 = createUser("13800138002", "用户B", "ACTIVE");
            Pet pet = createPet(user2.getId(), "旺财", "DOG");
            String token1 = jwtTokenService.signUserToken(user1.getId());

            mockMvc.perform(get("/api/v1/user/pets/" + pet.getId())
                            .header("Authorization", "Bearer " + token1))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("deleted pet detail returns 404")
        void deletedPetDetailReturns404() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            Pet pet = createPet(user.getId(), "团子", "CAT");
            petService.removeById(pet.getId());
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(get("/api/v1/user/pets/" + pet.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());
        }
    }

    // ======================== RED-2: Update Contract ========================

    @Nested
    @DisplayName("PUT /api/v1/user/pets/{petId} — update")
    class UpdatePet {

        @Test
        @DisplayName("user updates own pet")
        void userUpdatesOwnPet() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            Pet pet = createPet(user.getId(), "团子", "CAT");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(put("/api/v1/user/pets/" + pet.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"大团子\",\"type\":\"DOG\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("大团子"))
                    .andExpect(jsonPath("$.data.type").value("DOG"))
                    .andExpect(jsonPath("$.data.petId").value(String.valueOf(pet.getId())));
        }

        @Test
        @DisplayName("user cannot update other user's pet — 404, DB unchanged")
        void userCannotUpdateOtherUserPet() throws Exception {
            User user1 = createUser("13800138001", "用户A", "ACTIVE");
            User user2 = createUser("13800138002", "用户B", "ACTIVE");
            Pet pet = createPet(user2.getId(), "旺财", "DOG");
            String token1 = jwtTokenService.signUserToken(user1.getId());

            mockMvc.perform(put("/api/v1/user/pets/" + pet.getId())
                            .header("Authorization", "Bearer " + token1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"被改了\",\"type\":\"CAT\"}"))
                    .andExpect(status().isNotFound());

            Pet dbPet = petService.getById(pet.getId());
            assertThat(dbPet.getName()).isEqualTo("旺财");
        }

        @Test
        @DisplayName("request cannot modify userId, id, or deleted")
        void requestCannotModifyProtectedFields() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            Pet pet = createPet(user.getId(), "团子", "CAT");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(put("/api/v1/user/pets/" + pet.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"更新名\",\"type\":\"DOG\",\"userId\":999999,\"deleted\":1}"))
                    .andExpect(status().isOk());

            Pet dbPet = petService.getById(pet.getId());
            assertThat(dbPet.getUserId()).isEqualTo(user.getId());
            assertThat(dbPet.getDeleted()).isEqualTo(0);
            assertThat(dbPet.getName()).isEqualTo("更新名");
        }

        @Test
        @DisplayName("deleted pet update returns 404")
        void deletedPetUpdateReturns404() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            Pet pet = createPet(user.getId(), "团子", "CAT");
            petService.removeById(pet.getId());
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(put("/api/v1/user/pets/" + pet.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"更新\",\"type\":\"CAT\"}"))
                    .andExpect(status().isNotFound());
        }
    }

    // ======================== RED-2: Delete Contract ========================

    @Nested
    @DisplayName("DELETE /api/v1/user/pets/{petId} — delete")
    class DeletePet {

        @Test
        @DisplayName("user soft-deletes own pet")
        void userSoftDeletesOwnPet() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            Pet pet = createPet(user.getId(), "团子", "CAT");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(delete("/api/v1/user/pets/" + pet.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            assertThat(petService.getById(pet.getId())).isNull();
        }

        @Test
        @DisplayName("user cannot delete other user's pet — 404, DB unchanged")
        void userCannotDeleteOtherUserPet() throws Exception {
            User user1 = createUser("13800138001", "用户A", "ACTIVE");
            User user2 = createUser("13800138002", "用户B", "ACTIVE");
            Pet pet = createPet(user2.getId(), "旺财", "DOG");
            String token1 = jwtTokenService.signUserToken(user1.getId());

            mockMvc.perform(delete("/api/v1/user/pets/" + pet.getId())
                            .header("Authorization", "Bearer " + token1))
                    .andExpect(status().isNotFound());

            assertThat(petService.getById(pet.getId())).isNotNull();
        }

        @Test
        @DisplayName("already deleted pet delete returns 404")
        void alreadyDeletedPetDeleteReturns404() throws Exception {
            User user = createUser("13800138001", "宠物主人", "ACTIVE");
            Pet pet = createPet(user.getId(), "团子", "CAT");
            petService.removeById(pet.getId());
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(delete("/api/v1/user/pets/" + pet.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());
        }
    }

    // ======================== Authorization Boundary ========================

    @Nested
    @DisplayName("Authorization boundary")
    class AuthorizationBoundary {

        @Test
        @DisplayName("ADMIN token on pet endpoints returns 403")
        void adminTokenReturns403() throws Exception {
            Long adminId = createTestAdmin("pet_admin", "SUPER_ADMIN");
            String adminToken = jwtTokenService.signAdminToken(adminId, "pet_admin", "SUPER_ADMIN");

            mockMvc.perform(get("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/api/v1/user/pets")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"x\",\"type\":\"CAT\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("no token returns 401")
        void noTokenReturns401() throws Exception {
            mockMvc.perform(get("/api/v1/user/pets"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(post("/api/v1/user/pets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"x\",\"type\":\"CAT\"}"))
                    .andExpect(status().isUnauthorized());
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

    private Long createTestAdmin(String username, String role) {
        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode("password123456"));
        admin.setRole(role);
        admin.setStatus("ACTIVE");
        adminUserService.save(admin);
        return admin.getId();
    }

    private static String validPetJson(String name, String type) {
        return "{\"name\":\"" + name + "\",\"type\":\"" + type + "\"}";
    }
}
