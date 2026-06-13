package com.petcare.user.controller;

import com.petcare.admin.entity.AdminUser;
import com.petcare.admin.service.AdminUserService;
import com.petcare.common.security.JwtTokenService;
import com.petcare.user.entity.User;
import com.petcare.user.entity.UserAddress;
import com.petcare.user.service.UserAddressService;
import com.petcare.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RED-1: Integration tests for current user address API.
 * Contract defined in docs/39-phase-11-04-glm5-address-api-brief.md.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserAddressService addressService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ======================== RED-1: Create Contract ========================

    @Nested
    @DisplayName("POST /api/v1/user/addresses — create")
    class CreateAddress {

        @Test
        @DisplayName("USER creates first address, auto becomes default")
        void firstAddressAutoDefault() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validAddressJson()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.addressId").isString())
                    .andExpect(jsonPath("$.data.contactName").value("张三"))
                    .andExpect(jsonPath("$.data.isDefault").value(true));
        }

        @Test
        @DisplayName("DB userId from token, forged userId/id/deleted in request ignored")
        void forgedFieldsIgnored() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validAddressJsonWithForgedFields()))
                    .andExpect(status().isCreated());

            assertThat(addressService.list()).hasSize(1);
            UserAddress saved = addressService.list().get(0);
            assertThat(saved.getUserId()).isEqualTo(user.getId());
            assertThat(saved.getIsDefault()).isEqualTo(1);
            assertThat(saved.getDeleted()).isEqualTo(0);
        }

        @Test
        @DisplayName("addressId is String, response has no userId/deleted")
        void responseShape() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validAddressJson()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.addressId").isString())
                    .andExpect(jsonPath("$.data.userId").doesNotExist())
                    .andExpect(jsonPath("$.data.deleted").doesNotExist());
        }

        @Test
        @DisplayName("blank required field returns 400 validation_error")
        void blankRequiredFieldReturns400() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"contactName\":\"   \"," +
                                    "\"contactPhone\":\"13800138000\"," +
                                    "\"province\":\"广东省\"," +
                                    "\"city\":\"深圳市\"," +
                                    "\"detailAddress\":\"科技园1号\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("null required field returns 400 validation_error")
        void nullRequiredFieldReturns400() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"contactName\":\"张三\"," +
                                    "\"contactPhone\":\"13800138000\"," +
                                    "\"province\":\"广东省\"," +
                                    "\"city\":\"深圳市\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("longitude out of range returns 400")
        void longitudeOutOfRange() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validAddressJsonWithCoords(200.0, 22.5)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("latitude out of range returns 400")
        void latitudeOutOfRange() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validAddressJsonWithCoords(113.9, 100.0)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("longitude with more than 6 decimal places returns 400")
        void longitudeTooManyDecimals() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validAddressJsonWithCoords(113.9345281, 22.540503)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("only longitude without latitude returns 400")
        void onlyLongitudeWithoutLatitude() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{" +
                                    "\"contactName\":\"张三\"," +
                                    "\"contactPhone\":\"13800138000\"," +
                                    "\"province\":\"广东省\"," +
                                    "\"city\":\"深圳市\"," +
                                    "\"detailAddress\":\"科技园1号\"," +
                                    "\"longitude\":113.934528}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("only latitude without longitude returns 400")
        void onlyLatitudeWithoutLongitude() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{" +
                                    "\"contactName\":\"张三\"," +
                                    "\"contactPhone\":\"13800138000\"," +
                                    "\"province\":\"广东省\"," +
                                    "\"city\":\"深圳市\"," +
                                    "\"detailAddress\":\"科技园1号\"," +
                                    "\"latitude\":22.540503}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("contactName is trimmed before save")
        void contactNameIsTrimmed() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{" +
                                    "\"contactName\":\"  张三  \"," +
                                    "\"contactPhone\":\"13800138000\"," +
                                    "\"province\":\"广东省\"," +
                                    "\"city\":\"深圳市\"," +
                                    "\"detailAddress\":\"科技园1号\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.contactName").value("张三"));
        }

        @Test
        @DisplayName("blank district becomes null")
        void blankDistrictBecomesNull() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(post("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{" +
                                    "\"contactName\":\"张三\"," +
                                    "\"contactPhone\":\"13800138000\"," +
                                    "\"province\":\"广东省\"," +
                                    "\"city\":\"深圳市\"," +
                                    "\"district\":\"   \"," +
                                    "\"detailAddress\":\"科技园1号\"}"))
                    .andExpect(status().isCreated());

            UserAddress saved = addressService.list().get(0);
            assertThat(saved.getDistrict()).isNull();
        }
    }

    // ======================== RED-1: List Contract ========================

    @Nested
    @DisplayName("GET /api/v1/user/addresses — list")
    class ListAddresses {

        @Test
        @DisplayName("list only returns current user's non-deleted addresses")
        void listOnlyOwnNonDeleted() throws Exception {
            User user1 = createUser("13800138001", "用户A", "ACTIVE");
            User user2 = createUser("13800138002", "用户B", "ACTIVE");

            createAddress(user1.getId(), "张三", "13800138001");
            createAddress(user2.getId(), "李四", "13800138002");
            createAddress(user1.getId(), "王五", "13800138003");

            String token1 = jwtTokenService.signUserToken(user1.getId());

            mockMvc.perform(get("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + token1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[?(@.contactName=='张三')]").exists())
                    .andExpect(jsonPath("$.data[?(@.contactName=='王五')]").exists())
                    .andExpect(jsonPath("$.data[?(@.contactName=='李四')]").doesNotExist());
        }

        @Test
        @DisplayName("list excludes soft-deleted addresses")
        void listExcludesDeleted() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            UserAddress addr1 = createAddress(user.getId(), "张三", "13800138001");
            createAddress(user.getId(), "王五", "13800138003");

            addressService.removeById(addr1.getId());

            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(get("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].contactName").value("王五"));
        }

        @Test
        @DisplayName("list sorted by isDefault DESC, createTime DESC, id DESC")
        void listSortedCorrectly() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            // First address is the default
            UserAddress addr1 = createAddress(user.getId(), "地址1", "13800138001");
            addr1.setIsDefault(1);
            addressService.updateById(addr1);
            createAddress(user.getId(), "地址2", "13800138002");
            createAddress(user.getId(), "地址3", "13800138003");

            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(get("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(3))
                    .andExpect(jsonPath("$.data[0].isDefault").value(true))
                    .andExpect(jsonPath("$.data[0].contactName").value("地址1"));
        }

        @Test
        @DisplayName("empty list when user has no addresses")
        void emptyListWhenNoAddresses() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(get("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("list items: addressId is String, no userId/deleted")
        void listItemShape() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            createAddress(user.getId(), "张三", "13800138001");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(get("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].addressId").isString())
                    .andExpect(jsonPath("$.data[0].userId").doesNotExist())
                    .andExpect(jsonPath("$.data[0].deleted").doesNotExist());
        }
    }

    // ======================== Authorization Boundary ========================

    @Nested
    @DisplayName("Authorization boundary")
    class AuthorizationBoundary {

        @Test
        @DisplayName("ADMIN token on address endpoints returns 403")
        void adminTokenReturns403() throws Exception {
            Long adminId = createTestAdmin("addr_admin", "SUPER_ADMIN");
            String adminToken = jwtTokenService.signAdminToken(adminId, "addr_admin", "SUPER_ADMIN");

            mockMvc.perform(get("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validAddressJson()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("no token returns 401")
        void noTokenReturns401() throws Exception {
            mockMvc.perform(get("/api/v1/user/addresses"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(post("/api/v1/user/addresses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validAddressJson()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ======================== Ownership Boundary ========================

    @Nested
    @DisplayName("Ownership boundary")
    class OwnershipBoundary {

        @Test
        @DisplayName("user A cannot update user B's address — 404, DB unchanged")
        void userCannotUpdateOtherUserAddress() throws Exception {
            User user1 = createUser("13800138001", "用户A", "ACTIVE");
            User user2 = createUser("13800138002", "用户B", "ACTIVE");
            UserAddress addr = createAddress(user2.getId(), "李四", "13800138002");
            String token1 = jwtTokenService.signUserToken(user1.getId());

            mockMvc.perform(put("/api/v1/user/addresses/" + addr.getId())
                            .header("Authorization", "Bearer " + token1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validAddressJson()))
                    .andExpect(status().isNotFound());

            UserAddress dbAddr = addressService.getById(addr.getId());
            assertThat(dbAddr.getContactName()).isEqualTo("李四");
        }

        @Test
        @DisplayName("user A cannot delete user B's address — 404, DB unchanged")
        void userCannotDeleteOtherUserAddress() throws Exception {
            User user1 = createUser("13800138001", "用户A", "ACTIVE");
            User user2 = createUser("13800138002", "用户B", "ACTIVE");
            UserAddress addr = createAddress(user2.getId(), "李四", "13800138002");
            String token1 = jwtTokenService.signUserToken(user1.getId());

            mockMvc.perform(delete("/api/v1/user/addresses/" + addr.getId())
                            .header("Authorization", "Bearer " + token1))
                    .andExpect(status().isNotFound());

            assertThat(addressService.getById(addr.getId())).isNotNull();
        }
    }

    // ======================== RED-2: Update Contract ========================

    @Nested
    @DisplayName("PUT /api/v1/user/addresses/{addressId} — update")
    class UpdateAddress {

        @Test
        @DisplayName("user updates own address")
        void userUpdatesOwnAddress() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            UserAddress addr = createDefaultAddress(user.getId(), "张三", "13800138001");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(put("/api/v1/user/addresses/" + addr.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{" +
                                    "\"contactName\":\"李四\"," +
                                    "\"contactPhone\":\"13900139000\"," +
                                    "\"province\":\"北京市\"," +
                                    "\"city\":\"北京市\"," +
                                    "\"detailAddress\":\"朝阳区1号\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.contactName").value("李四"))
                    .andExpect(jsonPath("$.data.province").value("北京市"))
                    .andExpect(jsonPath("$.data.addressId").value(String.valueOf(addr.getId())));
        }

        @Test
        @DisplayName("request cannot modify userId, id, or deleted in update")
        void requestCannotModifyProtectedFields() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            UserAddress addr = createDefaultAddress(user.getId(), "张三", "13800138001");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(put("/api/v1/user/addresses/" + addr.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{" +
                                    "\"contactName\":\"更新名\"," +
                                    "\"contactPhone\":\"13800138000\"," +
                                    "\"province\":\"广东省\"," +
                                    "\"city\":\"深圳市\"," +
                                    "\"detailAddress\":\"科技园1号\"," +
                                    "\"userId\":999999,\"deleted\":1}"))
                    .andExpect(status().isOk());

            UserAddress dbAddr = addressService.getById(addr.getId());
            assertThat(dbAddr.getUserId()).isEqualTo(user.getId());
            assertThat(dbAddr.getDeleted()).isEqualTo(0);
            assertThat(dbAddr.getContactName()).isEqualTo("更新名");
        }

        @Test
        @DisplayName("deleted address update returns 404")
        void deletedAddressUpdateReturns404() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            UserAddress addr = createDefaultAddress(user.getId(), "张三", "13800138001");
            addressService.removeById(addr.getId());
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(put("/api/v1/user/addresses/" + addr.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validAddressJson()))
                    .andExpect(status().isNotFound());
        }
    }

    // ======================== RED-2: Delete Contract ========================

    @Nested
    @DisplayName("DELETE /api/v1/user/addresses/{addressId} — delete")
    class DeleteAddress {

        @Test
        @DisplayName("user soft-deletes own address")
        void userSoftDeletesOwnAddress() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            UserAddress addr = createDefaultAddress(user.getId(), "张三", "13800138001");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(delete("/api/v1/user/addresses/" + addr.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            assertThat(addressService.getById(addr.getId())).isNull();
        }

        @Test
        @DisplayName("already deleted address delete returns 404")
        void alreadyDeletedAddressDeleteReturns404() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            UserAddress addr = createDefaultAddress(user.getId(), "张三", "13800138001");
            addressService.removeById(addr.getId());
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(delete("/api/v1/user/addresses/" + addr.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());
        }
    }

    // ======================== RED-2: Default Address Invariant ========================

    @Nested
    @DisplayName("Default address invariant")
    class DefaultAddressInvariant {

        @Test
        @DisplayName("setting new default unsets old default")
        void settingNewDefaultUnsetsOld() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            createDefaultAddress(user.getId(), "地址1", "13800138001");
            String token = jwtTokenService.signUserToken(user.getId());

            // Create second address as default
            mockMvc.perform(post("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{" +
                                    "\"contactName\":\"地址2\"," +
                                    "\"contactPhone\":\"13800138002\"," +
                                    "\"province\":\"广东省\"," +
                                    "\"city\":\"深圳市\"," +
                                    "\"detailAddress\":\"科技园2号\"," +
                                    "\"isDefault\":true}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.isDefault").value(true));

            // Verify only one default
            List<UserAddress> all = addressService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserAddress>()
                    .eq(UserAddress::getUserId, user.getId()));
            long defaultCount = all.stream().filter(a -> a.getIsDefault() == 1).count();
            assertThat(defaultCount).isEqualTo(1);
        }

        @Test
        @DisplayName("updating current default to isDefault=false still keeps it default")
        void updatingDefaultToFalseKeepsDefault() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            UserAddress defaultAddr = createDefaultAddress(user.getId(), "默认地址", "13800138001");
            createAddress(user.getId(), "其他地址", "13800138002");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(put("/api/v1/user/addresses/" + defaultAddr.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{" +
                                    "\"contactName\":\"更新名\"," +
                                    "\"contactPhone\":\"13800138000\"," +
                                    "\"province\":\"广东省\"," +
                                    "\"city\":\"深圳市\"," +
                                    "\"detailAddress\":\"科技园1号\"," +
                                    "\"isDefault\":false}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.isDefault").value(true));

            // Verify still only one default
            UserAddress dbAddr = addressService.getById(defaultAddr.getId());
            assertThat(dbAddr.getIsDefault()).isEqualTo(1);
        }

        @Test
        @DisplayName("deleting non-default address does not change default")
        void deletingNonDefaultKeepsDefault() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            UserAddress defaultAddr = createDefaultAddress(user.getId(), "默认地址", "13800138001");
            UserAddress otherAddr = createAddress(user.getId(), "其他地址", "13800138002");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(delete("/api/v1/user/addresses/" + otherAddr.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            UserAddress dbDefault = addressService.getById(defaultAddr.getId());
            assertThat(dbDefault.getIsDefault()).isEqualTo(1);
        }

        @Test
        @DisplayName("deleting default address promotes next by createTime DESC, id DESC")
        void deletingDefaultPromotesNext() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            UserAddress defaultAddr = createDefaultAddress(user.getId(), "默认", "13800138001");
            // Create second and third — they'll have later createTime
            UserAddress addr2 = createAddress(user.getId(), "地址2", "13800138002");
            UserAddress addr3 = createAddress(user.getId(), "地址3", "13800138003");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(delete("/api/v1/user/addresses/" + defaultAddr.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            // addr3 should be promoted (latest createTime)
            UserAddress promoted = addressService.getById(addr3.getId());
            assertThat(promoted.getIsDefault()).isEqualTo(1);

            UserAddress other = addressService.getById(addr2.getId());
            assertThat(other.getIsDefault()).isEqualTo(0);
        }

        @Test
        @DisplayName("deleting last address leaves no addresses")
        void deletingLastAddressLeavesNone() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            UserAddress addr = createDefaultAddress(user.getId(), "唯一地址", "13800138001");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(delete("/api/v1/user/addresses/" + addr.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/user/addresses")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("updating other address to isDefault=true unsets old default")
        void updatingOtherToDefaultUnsetsOld() throws Exception {
            User user = createUser("13800138001", "用户A", "ACTIVE");
            UserAddress defaultAddr = createDefaultAddress(user.getId(), "默认", "13800138001");
            UserAddress otherAddr = createAddress(user.getId(), "其他", "13800138002");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(put("/api/v1/user/addresses/" + otherAddr.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{" +
                                    "\"contactName\":\"其他\"," +
                                    "\"contactPhone\":\"13800138002\"," +
                                    "\"province\":\"广东省\"," +
                                    "\"city\":\"深圳市\"," +
                                    "\"detailAddress\":\"科技园2号\"," +
                                    "\"isDefault\":true}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.isDefault").value(true));

            UserAddress oldDefault = addressService.getById(defaultAddr.getId());
            assertThat(oldDefault.getIsDefault()).isEqualTo(0);
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

    private UserAddress createAddress(Long userId, String contactName, String phone) {
        UserAddress addr = new UserAddress();
        addr.setUserId(userId);
        addr.setContactName(contactName);
        addr.setContactPhone(phone);
        addr.setProvince("广东省");
        addr.setCity("深圳市");
        addr.setDetailAddress("科技园1号");
        addr.setIsDefault(0);
        addressService.save(addr);
        return addr;
    }

    private UserAddress createDefaultAddress(Long userId, String contactName, String phone) {
        UserAddress addr = new UserAddress();
        addr.setUserId(userId);
        addr.setContactName(contactName);
        addr.setContactPhone(phone);
        addr.setProvince("广东省");
        addr.setCity("深圳市");
        addr.setDetailAddress("科技园1号");
        addr.setIsDefault(1);
        addressService.save(addr);
        return addr;
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

    private static String validAddressJson() {
        return "{" +
                "\"contactName\":\"张三\"," +
                "\"contactPhone\":\"13800138000\"," +
                "\"province\":\"广东省\"," +
                "\"city\":\"深圳市\"," +
                "\"district\":\"南山区\"," +
                "\"detailAddress\":\"科技园1号\"," +
                "\"longitude\":113.934528," +
                "\"latitude\":22.540503," +
                "\"isDefault\":true" +
                "}";
    }

    private static String validAddressJsonWithForgedFields() {
        return "{" +
                "\"contactName\":\"张三\"," +
                "\"contactPhone\":\"13800138000\"," +
                "\"province\":\"广东省\"," +
                "\"city\":\"深圳市\"," +
                "\"detailAddress\":\"科技园1号\"," +
                "\"userId\":999999," +
                "\"id\":999999," +
                "\"deleted\":1" +
                "}";
    }

    private static String validAddressJsonWithCoords(double lng, double lat) {
        return "{" +
                "\"contactName\":\"张三\"," +
                "\"contactPhone\":\"13800138000\"," +
                "\"province\":\"广东省\"," +
                "\"city\":\"深圳市\"," +
                "\"detailAddress\":\"科技园1号\"," +
                "\"longitude\":" + lng + "," +
                "\"latitude\":" + lat +
                "}";
    }
}
