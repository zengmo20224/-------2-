package com.petcare.moderation.domain;

import com.petcare.moderation.dto.ContentReviewResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ContentModerationPolicy pure domain logic.
 * Tests cover: no risk, level 1/2/3 content outcomes.
 */
class ContentModerationPolicyTest {

    @Nested
    @DisplayName("No sensitive words matched")
    class NoSensitiveWords {

        @Test
        @DisplayName("No matches: PUBLISHED status, APPROVED review, risk level 0")
        void noMatches_published() {
            ContentReviewResult result = ContentModerationPolicy.evaluate(List.of());
            assertEquals(0, result.riskLevel());
            assertEquals("PUBLISHED", result.contentStatus());
            assertEquals("APPROVED", result.reviewStatus());
            assertTrue(result.matchedWords().isEmpty());
        }
    }

    @Nested
    @DisplayName("Level 1 (mild) sensitive words")
    class Level1Content {

        @Test
        @DisplayName("Level 1: PENDING_REVIEW status, PENDING review")
        void level1_pendingReview() {
            List<MatchedSensitiveWord> words = List.of(
                    new MatchedSensitiveWord("广告", 1));
            ContentReviewResult result = ContentModerationPolicy.evaluate(words);
            assertEquals(1, result.riskLevel());
            assertEquals("PENDING_REVIEW", result.contentStatus());
            assertEquals("PENDING", result.reviewStatus());
        }
    }

    @Nested
    @DisplayName("Level 2 (moderate) sensitive words")
    class Level2Content {

        @Test
        @DisplayName("Level 2: PENDING_REVIEW status, PENDING review")
        void level2_pendingReview() {
            List<MatchedSensitiveWord> words = List.of(
                    new MatchedSensitiveWord("违禁品", 2));
            ContentReviewResult result = ContentModerationPolicy.evaluate(words);
            assertEquals(2, result.riskLevel());
            assertEquals("PENDING_REVIEW", result.contentStatus());
            assertEquals("PENDING", result.reviewStatus());
        }
    }

    @Nested
    @DisplayName("Level 3 (severe) sensitive words")
    class Level3Content {

        @Test
        @DisplayName("Level 3: REJECTED status, REJECTED review")
        void level3_rejected() {
            List<MatchedSensitiveWord> words = List.of(
                    new MatchedSensitiveWord("政治敏感", 3));
            ContentReviewResult result = ContentModerationPolicy.evaluate(words);
            assertEquals(3, result.riskLevel());
            assertEquals("REJECTED", result.contentStatus());
            assertEquals("REJECTED", result.reviewStatus());
        }
    }

    @Nested
    @DisplayName("Multi-word scenarios")
    class MultiWordScenarios {

        @Test
        @DisplayName("Mixed levels 1+2: PENDING_REVIEW, risk level 2")
        void mixed12_pendingReview() {
            List<MatchedSensitiveWord> words = List.of(
                    new MatchedSensitiveWord("广告", 1),
                    new MatchedSensitiveWord("违禁品", 2));
            ContentReviewResult result = ContentModerationPolicy.evaluate(words);
            assertEquals(2, result.riskLevel());
            assertEquals("PENDING_REVIEW", result.contentStatus());
            assertEquals("PENDING", result.reviewStatus());
        }

        @Test
        @DisplayName("Mixed levels 1+3: REJECTED, risk level 3")
        void mixed13_rejected() {
            List<MatchedSensitiveWord> words = List.of(
                    new MatchedSensitiveWord("广告", 1),
                    new MatchedSensitiveWord("政治敏感", 3));
            ContentReviewResult result = ContentModerationPolicy.evaluate(words);
            assertEquals(3, result.riskLevel());
            assertEquals("REJECTED", result.contentStatus());
            assertEquals("REJECTED", result.reviewStatus());
        }

        @Test
        @DisplayName("Mixed levels 2+3: REJECTED, risk level 3")
        void mixed23_rejected() {
            List<MatchedSensitiveWord> words = List.of(
                    new MatchedSensitiveWord("违禁品", 2),
                    new MatchedSensitiveWord("政治敏感", 3));
            ContentReviewResult result = ContentModerationPolicy.evaluate(words);
            assertEquals(3, result.riskLevel());
            assertEquals("REJECTED", result.contentStatus());
            assertEquals("REJECTED", result.reviewStatus());
        }

        @Test
        @DisplayName("All three levels: REJECTED, risk level 3")
        void allLevels_rejected() {
            List<MatchedSensitiveWord> words = List.of(
                    new MatchedSensitiveWord("广告", 1),
                    new MatchedSensitiveWord("违禁品", 2),
                    new MatchedSensitiveWord("政治敏感", 3));
            ContentReviewResult result = ContentModerationPolicy.evaluate(words);
            assertEquals(3, result.riskLevel());
            assertEquals("REJECTED", result.contentStatus());
            assertEquals("REJECTED", result.reviewStatus());
            assertEquals(3, result.matchedWords().size());
        }
    }
}
