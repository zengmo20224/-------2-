package com.petcare.ai.domain;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Deterministic high-risk pet symptom detector.
 * Must be checked BEFORE calling any AI Provider.
 * When a high-risk symptom is detected, the system returns a fixed
 * veterinary advice response without invoking the Provider.
 *
 * This is NOT a prompt-based safety measure — it uses pure Java rules.
 */
public final class HighRiskSymptomDetector {

    private static final List<Pattern> HIGH_RISK_PATTERNS = List.of(
            Pattern.compile("呕吐"),
            Pattern.compile("抽搐"),
            Pattern.compile("便血"),
            Pattern.compile("中毒"),
            Pattern.compile("呼吸困难"),
            Pattern.compile("长期不吃不喝"),
            Pattern.compile("不吃不喝"),
            Pattern.compile("误食异物"),
            Pattern.compile("严重外伤"),
            Pattern.compile("高烧"),
            Pattern.compile("骨折")
    );

    private static final String FIXED_SAFETY_RESPONSE =
            "⚠️ 检测到您的宠物可能存在紧急健康问题。本系统不能提供诊断或处方建议。"
            + "请尽快携带宠物前往正规宠物医院就诊。如有误食物品，请携带误食物或包装；"
            + "如有症状记录，请一并带给兽医参考。请不要自行用药。";

    private HighRiskSymptomDetector() {
        // prevent instantiation
    }

    /**
     * Checks if the user input contains any high-risk symptom keywords.
     *
     * @param userInput the user's message text
     * @return true if a high-risk symptom is detected
     */
    public static boolean isHighRisk(String userInput) {
        if (userInput == null || userInput.isBlank()) {
            return false;
        }
        for (Pattern pattern : HIGH_RISK_PATTERNS) {
            if (pattern.matcher(userInput).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the fixed safety response for high-risk symptoms.
     * This response never comes from an AI model.
     */
    public static String getFixedSafetyResponse() {
        return FIXED_SAFETY_RESPONSE;
    }
}
