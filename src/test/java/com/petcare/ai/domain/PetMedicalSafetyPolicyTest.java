package com.petcare.ai.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for post-processing medical safety policy on AI output.
 * Verifies that prohibited medical claims are detected and replaced.
 */
class PetMedicalSafetyPolicyTest {

    @Nested
    @DisplayName("Violation detection")
    class ViolationDetection {

        @Test
        @DisplayName("Detects diagnosis claims")
        void detectsDiagnosis() {
            assertTrue(PetMedicalSafetyPolicy.isViolation("你的猫诊断为了猫瘟"));
            assertTrue(PetMedicalSafetyPolicy.isViolation("你的猫诊断为猫瘟"));
            assertTrue(PetMedicalSafetyPolicy.isViolation("诊断结果是肠胃炎"));
            assertTrue(PetMedicalSafetyPolicy.isViolation("诊断可能是感冒了"));
        }

        @Test
        @DisplayName("Detects medication recommendations")
        void detectsMedication() {
            assertTrue(PetMedicalSafetyPolicy.isViolation("可以吃一点消炎药"));
            assertTrue(PetMedicalSafetyPolicy.isViolation("建议服用抗生素"));
            assertTrue(PetMedicalSafetyPolicy.isViolation("使用这种药物"));
        }

        @Test
        @DisplayName("Detects prescription references")
        void detectsPrescription() {
            assertTrue(PetMedicalSafetyPolicy.isViolation("我给你开个处方"));
        }

        @Test
        @DisplayName("Detects self-treatment encouragement")
        void detectsSelfTreatment() {
            assertTrue(PetMedicalSafetyPolicy.isViolation("可以自行用药"));
            assertTrue(PetMedicalSafetyPolicy.isViolation("可以自己处理一下"));
            assertTrue(PetMedicalSafetyPolicy.isViolation("可以自己治疗"));
        }

        @Test
        @DisplayName("Detects treatment guarantees")
        void detectsTreatmentGuarantee() {
            assertTrue(PetMedicalSafetyPolicy.isViolation("一定能治好"));
            assertTrue(PetMedicalSafetyPolicy.isViolation("保证康复"));
            assertTrue(PetMedicalSafetyPolicy.isViolation("一定能治愈"));
        }

        @Test
        @DisplayName("Detects vet discouragement")
        void detectsVetDiscouragement() {
            assertTrue(PetMedicalSafetyPolicy.isViolation("不需要去医院"));
            assertTrue(PetMedicalSafetyPolicy.isViolation("不用看兽医"));
        }

        @Test
        @DisplayName("Allows safe general advice")
        void allowsSafeAdvice() {
            assertFalse(PetMedicalSafetyPolicy.isViolation("建议咨询专业兽医"));
            assertFalse(PetMedicalSafetyPolicy.isViolation("你的猫咪看起来很健康"));
            assertFalse(PetMedicalSafetyPolicy.isViolation("多喝水，注意休息"));
            assertFalse(PetMedicalSafetyPolicy.isViolation("饮食均衡很重要"));
        }

        @Test
        @DisplayName("Null and blank input returns false")
        void nullBlankInput_returnsFalse() {
            assertFalse(PetMedicalSafetyPolicy.isViolation(null));
            assertFalse(PetMedicalSafetyPolicy.isViolation(""));
            assertFalse(PetMedicalSafetyPolicy.isViolation("   "));
        }
    }

    @Nested
    @DisplayName("Safe fallback")
    class SafeFallback {

        @Test
        @DisplayName("Fallback mentions no diagnosis or prescriptions")
        void fallback_noDiagnosisOrPrescriptions() {
            String fallback = PetMedicalSafetyPolicy.getSafeFallback();
            assertTrue(fallback.contains("诊断"));
            assertTrue(fallback.contains("处方"));
        }

        @Test
        @DisplayName("Fallback suggests consulting a vet")
        void fallback_suggestsVet() {
            String fallback = PetMedicalSafetyPolicy.getSafeFallback();
            assertTrue(fallback.contains("兽医"));
        }
    }
}
