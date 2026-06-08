package com.petcare.common.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.admin.entity.AdminPermission;
import com.petcare.admin.entity.AdminRole;
import com.petcare.admin.entity.AdminRolePermission;
import com.petcare.admin.mapper.AdminPermissionMapper;
import com.petcare.admin.mapper.AdminRoleMapper;
import com.petcare.admin.mapper.AdminRolePermissionMapper;
import com.petcare.ai.entity.AiConversation;
import com.petcare.ai.mapper.AiConversationMapper;
import com.petcare.booking.entity.ServiceBooking;
import com.petcare.booking.entity.StaffBookingLock;
import com.petcare.booking.mapper.ServiceBookingMapper;
import com.petcare.booking.mapper.StaffBookingLockMapper;
import com.petcare.community.entity.Topic;
import com.petcare.community.mapper.TopicMapper;
import com.petcare.marketing.entity.MarketingActivity;
import com.petcare.marketing.mapper.MarketingActivityMapper;
import com.petcare.moderation.entity.SensitiveWord;
import com.petcare.moderation.mapper.SensitiveWordMapper;
import com.petcare.product.entity.Product;
import com.petcare.product.entity.ProductCategory;
import com.petcare.product.mapper.ProductCategoryMapper;
import com.petcare.product.mapper.ProductMapper;
import com.petcare.service.entity.ServiceCategory;
import com.petcare.service.entity.ServiceItem;
import com.petcare.service.mapper.ServiceCategoryMapper;
import com.petcare.service.mapper.ServiceItemMapper;
import com.petcare.staff.entity.Staff;
import com.petcare.staff.mapper.StaffMapper;
import com.petcare.store.entity.Store;
import com.petcare.store.mapper.StoreMapper;
import com.petcare.user.entity.User;
import com.petcare.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("mysql-test")
@Transactional
class MySqlMapperIntegrationIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private ServiceCategoryMapper serviceCategoryMapper;

    @Autowired
    private ServiceItemMapper serviceItemMapper;

    @Autowired
    private StaffMapper staffMapper;

    @Autowired
    private ServiceBookingMapper serviceBookingMapper;

    @Autowired
    private StaffBookingLockMapper staffBookingLockMapper;

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;

    @Autowired
    private ProductCategoryMapper productCategoryMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private MarketingActivityMapper marketingActivityMapper;

    @Autowired
    private AiConversationMapper aiConversationMapper;

    @Autowired
    private AdminRoleMapper adminRoleMapper;

    @Autowired
    private AdminPermissionMapper adminPermissionMapper;

    @Autowired
    private AdminRolePermissionMapper adminRolePermissionMapper;

    @Test
    void connectsToMySql8() {
        String version = jdbcTemplate.queryForObject("select version()", String.class);

        assertNotNull(version);
        assertTrue(version.startsWith("8."), "Expected MySQL 8.x but got " + version);
    }

    @Test
    void everyMapperCanCountAgainstRealMysqlTables() {
        Arrays.stream(applicationContext.getBeanNamesForType(BaseMapper.class))
                .map(applicationContext::getBean)
                .map(bean -> (BaseMapper<?>) bean)
                .forEach(mapper -> assertDoesNotThrow(() -> mapper.selectCount(null)));
    }

    @Test
    void representativeModuleMappersInsertAndSelectOnMysql() {
        String suffix = Long.toString(System.nanoTime());

        User user = new User();
        user.setNickname("mysql_user_" + suffix);
        user.setStatus("ACTIVE");
        userMapper.insert(user);
        assertNotNull(userMapper.selectById(user.getId()));

        Store store = new Store();
        store.setStoreName("MySQL Store " + suffix);
        store.setStatus("OPEN");
        storeMapper.insert(store);
        assertNotNull(storeMapper.selectById(store.getId()));

        ServiceCategory serviceCategory = new ServiceCategory();
        serviceCategory.setName("MySQL Service Category " + suffix);
        serviceCategory.setStatus("ACTIVE");
        serviceCategoryMapper.insert(serviceCategory);

        ServiceItem serviceItem = new ServiceItem();
        serviceItem.setCategoryId(serviceCategory.getId());
        serviceItem.setName("MySQL Service Item " + suffix);
        serviceItem.setServiceMode("STORE");
        serviceItem.setPrice(new BigDecimal("88.00"));
        serviceItem.setDurationMinutes(60);
        serviceItem.setStatus("ON_SALE");
        serviceItemMapper.insert(serviceItem);

        Staff staff = new Staff();
        staff.setStoreId(store.getId());
        staff.setName("MySQL Staff " + suffix);
        staff.setRole("GROOMER");
        staff.setStatus("ACTIVE");
        staffMapper.insert(staff);

        ServiceBooking booking = new ServiceBooking();
        booking.setBookingNo("MYSQL-BK-" + suffix);
        booking.setUserId(user.getId());
        booking.setStoreId(store.getId());
        booking.setServiceItemId(serviceItem.getId());
        booking.setStaffId(staff.getId());
        booking.setServiceMode("STORE");
        booking.setBookingDate(LocalDate.of(2026, 6, 10));
        booking.setStartTime(LocalTime.of(10, 0));
        booking.setEndTime(LocalTime.of(11, 0));
        booking.setPrice(new BigDecimal("88.00"));
        booking.setPaymentStatus("UNPAID");
        booking.setStatus("PENDING_CONFIRM");
        serviceBookingMapper.insert(booking);
        assertNotNull(serviceBookingMapper.selectById(booking.getId()));

        Topic topic = new Topic();
        topic.setName("MySQL Topic " + suffix);
        topic.setStatus("ACTIVE");
        topicMapper.insert(topic);
        assertNotNull(topicMapper.selectById(topic.getId()));

        SensitiveWord sensitiveWord = new SensitiveWord();
        sensitiveWord.setWord("mysql-sensitive-" + suffix);
        sensitiveWord.setLevel(1);
        sensitiveWord.setStatus("ACTIVE");
        sensitiveWordMapper.insert(sensitiveWord);
        assertNotNull(sensitiveWordMapper.selectById(sensitiveWord.getId()));

        ProductCategory productCategory = new ProductCategory();
        productCategory.setName("MySQL Product Category " + suffix);
        productCategory.setStatus("ACTIVE");
        productCategoryMapper.insert(productCategory);

        Product product = new Product();
        product.setCategoryId(productCategory.getId());
        product.setName("MySQL Product " + suffix);
        product.setPrice(new BigDecimal("19.90"));
        product.setStock(10);
        product.setStatus("ON_SALE");
        productMapper.insert(product);
        assertNotNull(productMapper.selectById(product.getId()));

        MarketingActivity activity = new MarketingActivity();
        activity.setTitle("MySQL Activity " + suffix);
        activity.setActivityType("MIXED");
        activity.setStatus("DRAFT");
        marketingActivityMapper.insert(activity);
        assertNotNull(marketingActivityMapper.selectById(activity.getId()));

        AiConversation conversation = new AiConversation();
        conversation.setUserId(user.getId());
        conversation.setConversationType("CUSTOMER_SERVICE");
        conversation.setTitle("MySQL Conversation " + suffix);
        aiConversationMapper.insert(conversation);
        assertNotNull(aiConversationMapper.selectById(conversation.getId()));
    }

    @Test
    void uniqueConstraintsAreEnforcedOnMysql() {
        String suffix = Long.toString(System.nanoTime());

        StaffBookingLock lock = new StaffBookingLock();
        lock.setStaffId(900000000000000000L);
        lock.setBookingDate(LocalDate.of(2026, 6, 10));
        staffBookingLockMapper.insert(lock);

        StaffBookingLock duplicateLock = new StaffBookingLock();
        duplicateLock.setStaffId(lock.getStaffId());
        duplicateLock.setBookingDate(lock.getBookingDate());
        assertThrows(DuplicateKeyException.class, () -> staffBookingLockMapper.insert(duplicateLock));

        AdminRole role = new AdminRole();
        role.setRoleCode("MYSQL_ROLE_" + suffix);
        role.setRoleName("MySQL Role");
        role.setStatus("ACTIVE");
        adminRoleMapper.insert(role);

        AdminPermission permission = new AdminPermission();
        permission.setPermissionCode("mysql:resource:" + suffix);
        permission.setPermissionName("MySQL Permission");
        permission.setModule("mysql");
        permission.setStatus("ACTIVE");
        adminPermissionMapper.insert(permission);

        AdminRolePermission rolePermission = new AdminRolePermission();
        rolePermission.setRoleId(role.getId());
        rolePermission.setPermissionId(permission.getId());
        adminRolePermissionMapper.insert(rolePermission);

        AdminRolePermission duplicateRolePermission = new AdminRolePermission();
        duplicateRolePermission.setRoleId(role.getId());
        duplicateRolePermission.setPermissionId(permission.getId());
        assertThrows(DuplicateKeyException.class, () -> adminRolePermissionMapper.insert(duplicateRolePermission));
    }
}
