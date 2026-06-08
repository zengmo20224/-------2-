package com.petcare.common.persistence;

import com.petcare.booking.entity.StaffBookingLock;
import com.petcare.booking.mapper.StaffBookingLockMapper;
import com.petcare.booking.entity.ServiceBooking;
import com.petcare.booking.mapper.ServiceBookingMapper;
import com.petcare.staff.entity.Staff;
import com.petcare.staff.mapper.StaffMapper;
import com.petcare.store.entity.Store;
import com.petcare.store.mapper.StoreMapper;
import com.petcare.service.entity.ServiceCategory;
import com.petcare.service.mapper.ServiceCategoryMapper;
import com.petcare.service.entity.ServiceItem;
import com.petcare.service.mapper.ServiceItemMapper;
import com.petcare.user.entity.User;
import com.petcare.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies entity-table mapping and representative Mapper operations.
 */
@SpringBootTest
@ActiveProfiles("test")
class EntityMappingTest {

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

    @Test
    void serviceBookingMapperFullMapping() {
        // Create prerequisites
        User user = new User();
        user.setNickname("booking_user");
        user.setStatus("ACTIVE");
        userMapper.insert(user);

        Store store = new Store();
        store.setStoreName("Booking Store");
        store.setStatus("OPEN");
        storeMapper.insert(store);

        ServiceCategory category = new ServiceCategory();
        category.setName("Grooming");
        category.setStatus("ACTIVE");
        serviceCategoryMapper.insert(category);

        ServiceItem item = new ServiceItem();
        item.setCategoryId(category.getId());
        item.setName("Full Grooming");
        item.setServiceMode("STORE");
        item.setPrice(new BigDecimal("99.00"));
        item.setDurationMinutes(60);
        item.setStatus("ON_SALE");
        serviceItemMapper.insert(item);

        Staff staff = new Staff();
        staff.setStoreId(store.getId());
        staff.setName("John");
        staff.setRole("GROOMER");
        staff.setStatus("ACTIVE");
        staffMapper.insert(staff);

        // Create booking
        ServiceBooking booking = new ServiceBooking();
        booking.setBookingNo("BK20260608001");
        booking.setUserId(user.getId());
        booking.setStoreId(store.getId());
        booking.setServiceItemId(item.getId());
        booking.setStaffId(staff.getId());
        booking.setServiceMode("STORE");
        booking.setBookingDate(LocalDate.of(2026, 6, 10));
        booking.setStartTime(LocalTime.of(10, 0));
        booking.setEndTime(LocalTime.of(11, 0));
        booking.setPrice(new BigDecimal("99.00"));
        booking.setPaymentStatus("UNPAID");
        booking.setStatus("PENDING_CONFIRM");
        serviceBookingMapper.insert(booking);

        ServiceBooking found = serviceBookingMapper.selectById(booking.getId());
        assertNotNull(found);
        assertEquals("BK20260608001", found.getBookingNo());
        assertEquals("PENDING_CONFIRM", found.getStatus());
        assertEquals("UNPAID", found.getPaymentStatus());
        assertEquals(new BigDecimal("99.00"), found.getPrice());
        assertEquals(LocalDate.of(2026, 6, 10), found.getBookingDate());
        assertEquals(LocalTime.of(10, 0), found.getStartTime());
    }

    @Test
    void staffBookingLockUniqueConstraint() {
        Staff staff = new Staff();
        staff.setStoreId(1L);
        staff.setName("Lock Test Staff");
        staff.setRole("GROOMER");
        staff.setStatus("ACTIVE");
        staffMapper.insert(staff);

        LocalDate bookingDate = LocalDate.of(2026, 6, 10);

        StaffBookingLock lock1 = new StaffBookingLock();
        lock1.setStaffId(staff.getId());
        lock1.setBookingDate(bookingDate);
        staffBookingLockMapper.insert(lock1);

        assertNotNull(lock1.getId());

        // Second lock for same staff+date should fail due to unique constraint
        StaffBookingLock lock2 = new StaffBookingLock();
        lock2.setStaffId(staff.getId());
        lock2.setBookingDate(bookingDate);

        assertThrows(Exception.class, () -> staffBookingLockMapper.insert(lock2));
    }

    @Test
    void logicalDeleteWorks() {
        User user = new User();
        user.setNickname("delete_test_user");
        user.setStatus("ACTIVE");
        userMapper.insert(user);

        // Logical delete
        userMapper.deleteById(user.getId());

        // SelectById with @TableLogic should return null
        User found = userMapper.selectById(user.getId());
        assertNull(found);
    }
}
