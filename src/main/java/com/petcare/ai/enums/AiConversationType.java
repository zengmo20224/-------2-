package com.petcare.ai.enums;

import lombok.Getter;

/**
 * AI conversation type constants.
 * Matches schema.sql `ai_conversation.conversation_type`
 */
@Getter
public enum AiConversationType {
    CUSTOMER_SERVICE("CUSTOMER_SERVICE"),
    PET_CHAT("PET_CHAT"),
    ADMIN_ANALYSIS("ADMIN_ANALYSIS");

    private final String code;

    AiConversationType(String code) {
        this.code = code;
    }
}
