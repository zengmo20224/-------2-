package com.petcare.ai.domain;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Post-processing safety policy for AI pet chat output.
 * Checks the Provider response for prohibited medical claims
 * and replaces them with safe fallback text.
 *
 * This is a deterministic rule, not a prompt instruction.
 */
public final class PetMedicalSafetyPolicy {

    private static final List<Pattern> PROHIBITED_PATTERNS = List.of(
            Pattern.compile("诊断(?:为了?|结果是|应该是|可能是)"),
            Pattern.compile("(?:吃|服用|使用|喂).{0,10}(?:药|药物|抗生素|消炎药)"),
            Pattern.compile("处方"),
            Pattern.compile("可以(?:自行|自己).{0,6}(?:用药|治疗|处理)"),
            Pattern.compile("一定能?治[好疗愈]"),
            Pattern.compile("保证.{0,6}(?:康复|痊愈|好转)"),
            Pattern.compile("(?:不需要|不用).{0,6}(?:看兽医|去医院|就医)")
    );

    private static final String SAFE_FALLBACK =
            "作为宠物陪伴助手，我不能提供诊断、处方或治疗建议。"
            + "如果您的宠物有健康问题，请咨询专业兽医获取帮助。";

    private PetMedicalSafetyPolicy() {
        // prevent instantiation
    }

    /**
     * Checks if the AI output contains prohibited medical claims.
     *
     * @param aiOutput the text returned by the AI Provider
     * @return true if the output violates the medical safety policy
     */
    public static boolean isViolation(String aiOutput) {
        if (aiOutput == null || aiOutput.isBlank()) {
            return false;
        }
        for (Pattern pattern : PROHIBITED_PATTERNS) {
            if (pattern.matcher(aiOutput).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the safe fallback text when the output is rejected.
     */
    public static String getSafeFallback() {
        return SAFE_FALLBACK;
    }
}
