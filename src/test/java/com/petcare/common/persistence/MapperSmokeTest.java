package com.petcare.common.persistence;

import com.petcare.user.entity.User;
import com.petcare.user.mapper.UserMapper;
import com.petcare.store.entity.Store;
import com.petcare.store.mapper.StoreMapper;
import com.petcare.service.mapper.ServiceCategoryMapper;
import com.petcare.staff.mapper.StaffMapper;
import com.petcare.booking.mapper.ServiceBookingMapper;
import com.petcare.community.mapper.TopicMapper;
import com.petcare.moderation.mapper.SensitiveWordMapper;
import com.petcare.product.mapper.ProductMapper;
import com.petcare.marketing.mapper.MarketingActivityMapper;
import com.petcare.ai.mapper.AiConversationMapper;
import com.petcare.admin.entity.AdminUser;
import com.petcare.admin.mapper.AdminUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke test: verifies Spring context loads all Mappers and basic CRUD works.
 */
@SpringBootTest
@ActiveProfiles("test")
class MapperSmokeTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private ServiceCategoryMapper serviceCategoryMapper;

    @Autowired
    private StaffMapper staffMapper;

    @Autowired
    private ServiceBookingMapper serviceBookingMapper;

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private MarketingActivityMapper marketingActivityMapper;

    @Autowired
    private AiConversationMapper aiConversationMapper;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Test
    void contextLoads() {
        assertNotNull(userMapper);
        assertNotNull(storeMapper);
        assertNotNull(serviceCategoryMapper);
        assertNotNull(staffMapper);
        assertNotNull(serviceBookingMapper);
        assertNotNull(topicMapper);
        assertNotNull(sensitiveWordMapper);
        assertNotNull(productMapper);
        assertNotNull(marketingActivityMapper);
        assertNotNull(aiConversationMapper);
        assertNotNull(adminUserMapper);
    }

    @Test
    void userMapperInsertAndSelect() {
        User user = new User();
        user.setNickname("test_user");
        user.setStatus("ACTIVE");
        userMapper.insert(user);

        User found = userMapper.selectById(user.getId());
        assertNotNull(found);
        assertNotNull(found.getId());
        assertNotNull(found.getCreateTime());
        assertNotNull(found.getUpdateTime());
    }

    @Test
    void storeMapperInsertAndSelect() {
        Store store = new Store();
        store.setStoreName("Test Store");
        store.setStatus("OPEN");
        storeMapper.insert(store);

        Store found = storeMapper.selectById(store.getId());
        assertNotNull(found);
        assertNotNull(found.getId());
    }

    @Test
    void adminUserMapperInsertAndSelect() {
        AdminUser admin = new AdminUser();
        admin.setUsername("test_admin");
        admin.setPassword("hashed_password");
        admin.setRole("STAFF");
        admin.setStatus("ACTIVE");
        adminUserMapper.insert(admin);

        AdminUser found = adminUserMapper.selectById(admin.getId());
        assertNotNull(found);
        assertNotNull(found.getId());
        assertNotNull(found.getCreateTime());
    }
}
