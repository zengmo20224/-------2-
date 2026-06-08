package com.petcare.common.constant;

import com.petcare.common.enums.UserStatus;
import com.petcare.common.enums.StoreStatus;
import com.petcare.common.enums.ServiceItemStatus;
import com.petcare.common.enums.StaffStatus;
import com.petcare.booking.enums.ScheduleStatus;
import com.petcare.booking.enums.BookingStatus;
import com.petcare.booking.enums.PaymentStatus;
import com.petcare.booking.enums.PaymentMethod;
import com.petcare.community.enums.ContentStatus;
import com.petcare.product.enums.ProductOrderStatus;
import com.petcare.product.enums.PickupStatus;
import com.petcare.marketing.enums.ActivityStatus;
import com.petcare.admin.enums.AdminRoleCode;
import com.petcare.ai.enums.AiConversationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies status enum default values match schema.sql defaults.
 */
class StatusConstantTest {

    @Test
    void userStatusDefaults() {
        assertEquals("ACTIVE", UserStatus.ACTIVE.getCode());
        assertEquals("DISABLED", UserStatus.DISABLED.getCode());
    }

    @Test
    void storeStatusDefaults() {
        assertEquals("OPEN", StoreStatus.OPEN.getCode());
        assertEquals("CLOSED", StoreStatus.CLOSED.getCode());
    }

    @Test
    void serviceItemStatusDefaults() {
        assertEquals("ON_SALE", ServiceItemStatus.ON_SALE.getCode());
        assertEquals("OFF_SALE", ServiceItemStatus.OFF_SALE.getCode());
    }

    @Test
    void staffStatusDefaults() {
        assertEquals("ACTIVE", StaffStatus.ACTIVE.getCode());
        assertEquals("INACTIVE", StaffStatus.INACTIVE.getCode());
    }

    @Test
    void scheduleStatusDefaults() {
        assertEquals("AVAILABLE", ScheduleStatus.AVAILABLE.getCode());
        assertEquals("UNAVAILABLE", ScheduleStatus.UNAVAILABLE.getCode());
    }

    @Test
    void bookingStatusDefaults() {
        assertEquals("PENDING_CONFIRM", BookingStatus.PENDING_CONFIRM.getCode());
        assertEquals("CONFIRMED", BookingStatus.CONFIRMED.getCode());
        assertEquals("IN_SERVICE", BookingStatus.IN_SERVICE.getCode());
        assertEquals("COMPLETED", BookingStatus.COMPLETED.getCode());
        assertEquals("CANCELLED", BookingStatus.CANCELLED.getCode());
        assertEquals("REJECTED", BookingStatus.REJECTED.getCode());
    }

    @Test
    void paymentStatusDefaults() {
        assertEquals("UNPAID", PaymentStatus.UNPAID.getCode());
        assertEquals("OFFLINE_PAID", PaymentStatus.OFFLINE_PAID.getCode());
        assertEquals("REFUNDED", PaymentStatus.REFUNDED.getCode());
    }

    @Test
    void paymentMethodValues() {
        assertEquals("OFFLINE_STORE", PaymentMethod.OFFLINE_STORE.getCode());
        assertEquals("OFFLINE_HOME", PaymentMethod.OFFLINE_HOME.getCode());
        assertEquals("ONLINE_WECHAT", PaymentMethod.ONLINE_WECHAT.getCode());
        assertEquals("FREE", PaymentMethod.FREE.getCode());
    }

    @Test
    void contentStatusDefaults() {
        assertEquals("PUBLISHED", ContentStatus.PUBLISHED.getCode());
        assertEquals("PENDING_REVIEW", ContentStatus.PENDING_REVIEW.getCode());
        assertEquals("REJECTED", ContentStatus.REJECTED.getCode());
        assertEquals("HIDDEN", ContentStatus.HIDDEN.getCode());
        assertEquals("DELETED", ContentStatus.DELETED.getCode());
    }

    @Test
    void productOrderStatusDefaults() {
        assertEquals("PENDING_CONFIRM", ProductOrderStatus.PENDING_CONFIRM.getCode());
        assertEquals("PREPARING", ProductOrderStatus.PREPARING.getCode());
        assertEquals("READY_FOR_PICKUP", ProductOrderStatus.READY_FOR_PICKUP.getCode());
        assertEquals("COMPLETED", ProductOrderStatus.COMPLETED.getCode());
        assertEquals("CANCELLED", ProductOrderStatus.CANCELLED.getCode());
        assertEquals("OUT_OF_STOCK", ProductOrderStatus.OUT_OF_STOCK.getCode());
    }

    @Test
    void pickupStatusDefaults() {
        assertEquals("WAIT_PREPARE", PickupStatus.WAIT_PREPARE.getCode());
        assertEquals("READY_FOR_PICKUP", PickupStatus.READY_FOR_PICKUP.getCode());
        assertEquals("PICKED_UP", PickupStatus.PICKED_UP.getCode());
    }

    @Test
    void activityStatusDefaults() {
        assertEquals("DRAFT", ActivityStatus.DRAFT.getCode());
        assertEquals("ACTIVE", ActivityStatus.ACTIVE.getCode());
        assertEquals("ENDED", ActivityStatus.ENDED.getCode());
        assertEquals("CANCELLED", ActivityStatus.CANCELLED.getCode());
    }

    @Test
    void adminRoleCodeDefaults() {
        assertEquals("SUPER_ADMIN", AdminRoleCode.SUPER_ADMIN.getCode());
        assertEquals("MANAGER", AdminRoleCode.MANAGER.getCode());
        assertEquals("STAFF", AdminRoleCode.STAFF.getCode());
    }

    @Test
    void aiConversationTypeValues() {
        assertEquals("CUSTOMER_SERVICE", AiConversationType.CUSTOMER_SERVICE.getCode());
        assertEquals("PET_CHAT", AiConversationType.PET_CHAT.getCode());
        assertEquals("ADMIN_ANALYSIS", AiConversationType.ADMIN_ANALYSIS.getCode());
    }
}
