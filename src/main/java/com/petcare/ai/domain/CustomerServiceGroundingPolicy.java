package com.petcare.ai.domain;

/**
 * Grounding policy for AI customer service.
 * Ensures the AI response is grounded in verified business data.
 * When no trusted context is available, returns a fixed fallback.
 */
public final class CustomerServiceGroundingPolicy {

    private static final String NO_CONTEXT_FALLBACK =
            "当前资料中没有该信息，请联系门店确认。";

    private static final String[] PRICE_STOCK_KEYWORDS = {
            "价格", "多少钱", "费用", "收费",
            "库存", "有货", "还有没有",
            "营业时间", "几点开门", "几点关门", "上班时间",
            "服务范围", "能上门吗", "多远",
            "取消", "退款", "预约规则"
    };

    private CustomerServiceGroundingPolicy() {
        // prevent instantiation
    }

    /**
     * Returns the fixed fallback when no trusted context is available.
     */
    public static String getNoContextFallback() {
        return NO_CONTEXT_FALLBACK;
    }

    /**
     * Checks if the user question is about business facts that require grounding.
     *
     * @param userQuestion the user's question text
     * @return true if the question needs grounded data to answer
     */
    public static boolean requiresGrounding(String userQuestion) {
        if (userQuestion == null || userQuestion.isBlank()) {
            return false;
        }
        for (String keyword : PRICE_STOCK_KEYWORDS) {
            if (userQuestion.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the AI output claims specific business facts not present in the context.
     * Simple heuristic check — production would use more sophisticated validation.
     *
     * @param aiOutput the AI response text
     * @param context the grounded context
     * @return true if the output appears to fabricate business facts
     */
    public static boolean isFabricatedBusinessFact(String aiOutput, CustomerServiceContext context) {
        if (aiOutput == null || context == null || !context.hasData()) {
            return false;
        }
        // If context has no services but output mentions specific service prices
        if (context.services().isEmpty() && aiOutput.contains("元") && containsServiceKeywords(aiOutput)) {
            return true;
        }
        return false;
    }

    private static boolean containsServiceKeywords(String text) {
        String[] keywords = {"洗护", "美容", "寄养", "洗澡", "剪毛"};
        for (String kw : keywords) {
            if (text.contains(kw)) {
                return true;
            }
        }
        return false;
    }
}
