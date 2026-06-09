package com.petcare.ai.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for customer service grounding policy.
 * Ensures AI responses are grounded in verified business data.
 */
class CustomerServiceGroundingPolicyTest {

    @Nested
    @DisplayName("No-context fallback")
    class NoContextFallback {

        @Test
        @DisplayName("Fallback tells user to contact store")
        void fallback_contactStore() {
            String fallback = CustomerServiceGroundingPolicy.getNoContextFallback();
            assertTrue(fallback.contains("门店"));
            assertTrue(fallback.contains("联系"));
        }
    }

    @Nested
    @DisplayName("Grounding requirement detection")
    class GroundingRequirement {

        @Test
        @DisplayName("Price questions require grounding")
        void priceQuestions_requireGrounding() {
            assertTrue(CustomerServiceGroundingPolicy.requiresGrounding("洗澡多少钱"));
            assertTrue(CustomerServiceGroundingPolicy.requiresGrounding("价格是多少"));
            assertTrue(CustomerServiceGroundingPolicy.requiresGrounding("收费怎么样"));
            assertTrue(CustomerServiceGroundingPolicy.requiresGrounding("费用"));
        }

        @Test
        @DisplayName("Stock questions require grounding")
        void stockQuestions_requireGrounding() {
            assertTrue(CustomerServiceGroundingPolicy.requiresGrounding("有库存吗"));
            assertTrue(CustomerServiceGroundingPolicy.requiresGrounding("还有没有猫粮"));
            assertTrue(CustomerServiceGroundingPolicy.requiresGrounding("有货吗"));
        }

        @Test
        @DisplayName("Business hours questions require grounding")
        void businessHoursQuestions_requireGrounding() {
            assertTrue(CustomerServiceGroundingPolicy.requiresGrounding("营业时间是什么"));
            assertTrue(CustomerServiceGroundingPolicy.requiresGrounding("几点开门"));
            assertTrue(CustomerServiceGroundingPolicy.requiresGrounding("几点关门"));
            assertTrue(CustomerServiceGroundingPolicy.requiresGrounding("上班时间"));
        }

        @Test
        @DisplayName("Service range questions require grounding")
        void serviceRangeQuestions_requireGrounding() {
            assertTrue(CustomerServiceGroundingPolicy.requiresGrounding("能上门吗"));
            assertTrue(CustomerServiceGroundingPolicy.requiresGrounding("服务范围"));
            assertTrue(CustomerServiceGroundingPolicy.requiresGrounding("多远能上门"));
        }

        @Test
        @DisplayName("Cancellation questions require grounding")
        void cancellationQuestions_requireGrounding() {
            assertTrue(CustomerServiceGroundingPolicy.requiresGrounding("取消预约"));
            assertTrue(CustomerServiceGroundingPolicy.requiresGrounding("退款规则"));
        }

        @Test
        @DisplayName("General questions do not require grounding")
        void generalQuestions_noGrounding() {
            assertFalse(CustomerServiceGroundingPolicy.requiresGrounding("你好"));
            assertFalse(CustomerServiceGroundingPolicy.requiresGrounding("谢谢"));
            assertFalse(CustomerServiceGroundingPolicy.requiresGrounding("你们提供什么服务"));
        }

        @Test
        @DisplayName("Null and blank input returns false")
        void nullBlankInput_returnsFalse() {
            assertFalse(CustomerServiceGroundingPolicy.requiresGrounding(null));
            assertFalse(CustomerServiceGroundingPolicy.requiresGrounding(""));
            assertFalse(CustomerServiceGroundingPolicy.requiresGrounding("   "));
        }
    }

    @Nested
    @DisplayName("Fabrication detection")
    class FabricationDetection {

        @Test
        @DisplayName("Detects fabricated service prices when no services in context")
        void detectsFabricatedServicePrices() {
            CustomerServiceContext emptyServicesContext = new CustomerServiceContext(
                    "测试门店", null, null, null, null, null,
                    List.of(), List.of(), List.of(), true
            );

            assertTrue(CustomerServiceGroundingPolicy.isFabricatedBusinessFact(
                    "洗护服务80元，美容服务120元", emptyServicesContext));
        }

        @Test
        @DisplayName("No fabrication when context has services")
        void noFabrication_withServices() {
            CustomerServiceContext contextWithServices = new CustomerServiceContext(
                    "测试门店", null, null, null, null, null,
                    List.of(new CustomerServiceContext.ServiceItemFact(
                            1L, "洗护", "到店", "基础洗护", "80", "60", "IN_STORE")),
                    List.of(), List.of(), true
            );

            assertFalse(CustomerServiceGroundingPolicy.isFabricatedBusinessFact(
                    "洗护服务80元", contextWithServices));
        }
    }
}
