package com.petcare.moderation.domain;

import com.petcare.moderation.dto.ContentReviewResult;
import java.util.List;

/**
 * Pure policy for deciding content moderation outcomes.
 * No database access — receives matched words and produces a result.
 *
 * Rules:
 * - No matches: PUBLISHED / APPROVED, risk_level = 0
 * - Max level 1: PENDING_REVIEW / PENDING
 * - Max level 2: PENDING_REVIEW / PENDING
 * - Max level 3: REJECTED / REJECTED
 */
public final class ContentModerationPolicy {

    private ContentModerationPolicy() {
        // prevent instantiation
    }

    /**
     * Determines the moderation result based on matched sensitive words.
     *
     * @param matchedWords the list of matched sensitive words (may be empty)
     * @return content review result with status decisions
     */
    public static ContentReviewResult evaluate(List<MatchedSensitiveWord> matchedWords) {
        int riskLevel = SensitiveWordMatcher.calculateRiskLevel(matchedWords);

        return switch (riskLevel) {
            case 0 -> new ContentReviewResult(
                    0,
                    "PUBLISHED",
                    "APPROVED",
                    matchedWords
            );
            case 1 -> new ContentReviewResult(
                    1,
                    "PENDING_REVIEW",
                    "PENDING",
                    matchedWords
            );
            case 2 -> new ContentReviewResult(
                    2,
                    "PENDING_REVIEW",
                    "PENDING",
                    matchedWords
            );
            default -> new ContentReviewResult(
                    riskLevel,
                    "REJECTED",
                    "REJECTED",
                    matchedWords
            );
        };
    }
}
