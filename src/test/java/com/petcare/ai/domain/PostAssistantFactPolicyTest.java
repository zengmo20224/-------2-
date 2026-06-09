package com.petcare.ai.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for post assistant fact policy.
 * Ensures AI only uses facts explicitly provided by the user.
 */
class PostAssistantFactPolicyTest {

    @Nested
    @DisplayName("Allowed fact fields")
    class AllowedFields {

        @Test
        @DisplayName("Contains expected fact fields")
        void containsExpectedFields() {
            Set<String> fields = PostAssistantFactPolicy.getAllowedFactFields();
            assertTrue(fields.contains("petName"));
            assertTrue(fields.contains("petType"));
            assertTrue(fields.contains("event"));
            assertTrue(fields.contains("tone"));
            assertTrue(fields.contains("originalText"));
        }

        @Test
        @DisplayName("Does not contain forbidden fields")
        void noForbiddenFields() {
            Set<String> fields = PostAssistantFactPolicy.getAllowedFactFields();
            assertFalse(fields.contains("diagnosis"));
            assertFalse(fields.contains("medication"));
            assertFalse(fields.contains("breed"));
            assertFalse(fields.contains("age"));
        }
    }

    @Nested
    @DisplayName("System instruction")
    class SystemInstruction {

        @Test
        @DisplayName("Instructs not to fabricate")
        void instructsNoFabrication() {
            String instruction = PostAssistantFactPolicy.getSystemInstruction();
            assertTrue(instruction.contains("编造"));
            assertTrue(instruction.contains("用户明确提供"));
        }

        @Test
        @DisplayName("Instructs draft only")
        void instructsDraftOnly() {
            String instruction = PostAssistantFactPolicy.getSystemInstruction();
            assertTrue(instruction.contains("建议稿") || instruction.contains("确认"));
        }
    }
}
