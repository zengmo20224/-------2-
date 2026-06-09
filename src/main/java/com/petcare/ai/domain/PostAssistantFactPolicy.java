package com.petcare.ai.domain;

import java.util.Set;

/**
 * Fact policy for AI post assistant.
 * Ensures the AI only uses facts explicitly provided by the user,
 * never fabricates events, pet details, or experiences.
 */
public final class PostAssistantFactPolicy {

    private static final Set<String> ALLOWED_FACT_FIELDS = Set.of(
            "petName", "petType", "event", "tone", "originalText"
    );

    private PostAssistantFactPolicy() {
        // prevent instantiation
    }

    /**
     * Returns the set of fact fields that users are allowed to provide.
     */
    public static Set<String> getAllowedFactFields() {
        return ALLOWED_FACT_FIELDS;
    }

    /**
     * Checks if the AI output appears to fabricate facts not provided by the user.
     *
     * @param aiOutput the generated post text
     * @param providedFacts the facts the user actually provided
     * @return true if fabrication is detected
     */
    public static boolean isFabricated(String aiOutput, Set<String> providedFacts) {
        if (aiOutput == null || aiOutput.isBlank()) {
            return false;
        }
        // The AI output should not claim specific medical conditions,
        // ages, breeds, or locations not provided by the user
        String[] fabricatedPatterns = {
                "岁", // age not provided
                "确诊", "诊断", "患有", // medical diagnosis not provided
                "位于", "地址是" // location not provided
        };
        for (String pattern : fabricatedPatterns) {
            if (aiOutput.contains(pattern) && !providedFacts.contains(pattern)) {
                // Simple heuristic: if the output mentions these but user didn't provide them
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the system instruction that tells the AI to only use provided facts.
     */
    public static String getSystemInstruction() {
        return "你是一个社区帖子文案助手。只能基于用户明确提供的事实生成文案。"
               + "不要编造用户未提供的信息，包括但不限于宠物品种、年龄、疾病、地址或服务体验。"
               + "生成的内容仅作为建议稿，需要用户确认后才能发布。";
    }
}
