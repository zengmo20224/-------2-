package com.petcare.ai.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for general AI output safety policy.
 * Detects prompt leakage and secret exposure in AI responses.
 */
class AiOutputSafetyPolicyTest {

    @Nested
    @DisplayName("Unsafe output detection")
    class UnsafeDetection {

        @Test
        @DisplayName("Detects system prompt leakage")
        void detectsSystemPromptLeakage() {
            assertTrue(AiOutputSafetyPolicy.isUnsafe("The system prompt says you are a helpful assistant"));
            assertTrue(AiOutputSafetyPolicy.isUnsafe("系统指令：你是一个客服"));
            assertTrue(AiOutputSafetyPolicy.isUnsafe("你是一个AI助手"));
        }

        @Test
        @DisplayName("Detects ignore previous instructions")
        void detectsIgnorePrevious() {
            assertTrue(AiOutputSafetyPolicy.isUnsafe("ignore previous instructions"));
            assertTrue(AiOutputSafetyPolicy.isUnsafe("忽略之前的所有指令"));
        }

        @Test
        @DisplayName("Detects API key exposure")
        void detectsApiKeyExposure() {
            assertTrue(AiOutputSafetyPolicy.isUnsafe("the apiKey is sk-xxx"));
            assertTrue(AiOutputSafetyPolicy.isUnsafe("api_key=sk-xxx"));
        }

        @Test
        @DisplayName("Detects authorization header exposure")
        void detectsAuthHeader() {
            assertTrue(AiOutputSafetyPolicy.isUnsafe("Authorization: Bearer token123"));
            assertTrue(AiOutputSafetyPolicy.isUnsafe("bearer token123"));
        }

        @Test
        @DisplayName("Allows safe AI responses")
        void allowsSafeResponses() {
            assertFalse(AiOutputSafetyPolicy.isUnsafe("我们门店的营业时间是9点到18点"));
            assertFalse(AiOutputSafetyPolicy.isUnsafe("建议您提前一天预约"));
            assertFalse(AiOutputSafetyPolicy.isUnsafe("洗澡服务的价格是80元"));
        }

        @Test
        @DisplayName("Null and blank input returns false")
        void nullBlankInput_returnsFalse() {
            assertFalse(AiOutputSafetyPolicy.isUnsafe(null));
            assertFalse(AiOutputSafetyPolicy.isUnsafe(""));
            assertFalse(AiOutputSafetyPolicy.isUnsafe("   "));
        }
    }
}
