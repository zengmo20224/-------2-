package com.petcare.moderation.dto;

import com.petcare.moderation.domain.MatchedSensitiveWord;
import java.util.List;

/**
 * Result of content moderation analysis.
 * Immutable value object carrying the moderation decision.
 */
public record ContentReviewResult(
        int riskLevel,
        String contentStatus,
        String reviewStatus,
        List<MatchedSensitiveWord> matchedWords
) {
}
