package com.petcare.ai.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for high-risk pet symptom detection.
 * Every high-risk keyword must be tested individually.
 * Uses deterministic Java rules — no AI Provider involved.
 */
class HighRiskSymptomDetectorTest {

    @Nested
    @DisplayName("High-risk symptom detection")
    class HighRiskDetection {

        @ParameterizedTest(name = "Detects: {0}")
        @DisplayName("Each high-risk symptom triggers detection")
        @ValueSource(strings = {
                "我的猫呕吐了", "狗在抽搐", "狗狗便血了", "可能是中毒",
                "呼吸困难", "长期不吃不喝", "不吃不喝好几天了",
                "误食异物", "严重外伤", "高烧不退", "骨折了"
        })
        void detectsHighRisk(String input) {
            assertTrue(HighRiskSymptomDetector.isHighRisk(input),
                    "Should detect high risk in: " + input);
        }

        @Test
        @DisplayName("Null input returns false")
        void nullInput_returnsFalse() {
            assertFalse(HighRiskSymptomDetector.isHighRisk(null));
        }

        @Test
        @DisplayName("Blank input returns false")
        void blankInput_returnsFalse() {
            assertFalse(HighRiskSymptomDetector.isHighRisk("   "));
        }

        @Test
        @DisplayName("Normal input returns false")
        void normalInput_returnsFalse() {
            assertFalse(HighRiskSymptomDetector.isHighRisk("我的狗狗今天很开心"));
            assertFalse(HighRiskSymptomDetector.isHighRisk("猫咪想吃罐头"));
            assertFalse(HighRiskSymptomDetector.isHighRisk("今天天气怎么样"));
        }
    }

    @Nested
    @DisplayName("Fixed safety response")
    class FixedSafetyResponse {

        @Test
        @DisplayName("Response mentions veterinary care")
        void response_mentionsVet() {
            String response = HighRiskSymptomDetector.getFixedSafetyResponse();
            assertTrue(response.contains("宠物医院") || response.contains("兽医"));
        }

        @Test
        @DisplayName("Response states no diagnosis capability")
        void response_noDiagnosis() {
            String response = HighRiskSymptomDetector.getFixedSafetyResponse();
            assertTrue(response.contains("诊断") || response.contains("处方"));
        }

        @Test
        @DisplayName("Response advises not to self-medicate")
        void response_noSelfMedicate() {
            String response = HighRiskSymptomDetector.getFixedSafetyResponse();
            assertTrue(response.contains("自行用药") || response.contains("不要"));
        }

        @Test
        @DisplayName("Response is consistent across calls")
        void response_consistent() {
            assertEquals(
                    HighRiskSymptomDetector.getFixedSafetyResponse(),
                    HighRiskSymptomDetector.getFixedSafetyResponse()
            );
        }
    }
}
