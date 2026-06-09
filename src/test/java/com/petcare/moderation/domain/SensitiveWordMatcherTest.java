package com.petcare.moderation.domain;

import com.petcare.moderation.domain.SensitiveWordMatcher.SensitiveWordEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SensitiveWordMatcher pure domain logic.
 * Tests cover: no matches, level 1/2/3 matches, multi-word matches,
 * case normalization, null handling, and safe truncation.
 */
class SensitiveWordMatcherTest {

    private final SensitiveWordEntry level1Word = new SensitiveWordEntry("广告", 1);
    private final SensitiveWordEntry level2Word = new SensitiveWordEntry("违禁品", 2);
    private final SensitiveWordEntry level3Word = new SensitiveWordEntry("政治敏感", 3);

    @Nested
    @DisplayName("No match scenarios")
    class NoMatchScenarios {

        @Test
        @DisplayName("No match when sensitive word list is empty")
        void noMatch_emptyWordList() {
            List<MatchedSensitiveWord> result = SensitiveWordMatcher.match("一些正常文本", List.of());
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("No match when sensitive word list is null")
        void noMatch_nullWordList() {
            List<MatchedSensitiveWord> result = SensitiveWordMatcher.match("一些正常文本", null);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("No match when text is null")
        void noMatch_nullText() {
            List<MatchedSensitiveWord> result = SensitiveWordMatcher.match(null, List.of(level1Word));
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("No match when text is empty")
        void noMatch_emptyText() {
            List<MatchedSensitiveWord> result = SensitiveWordMatcher.match("", List.of(level1Word));
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("No match when text is only whitespace")
        void noMatch_whitespaceText() {
            List<MatchedSensitiveWord> result = SensitiveWordMatcher.match("   ", List.of(level1Word));
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("No match when text does not contain any sensitive word")
        void noMatch_normalText() {
            List<MatchedSensitiveWord> result = SensitiveWordMatcher.match(
                    "今天天气真好", List.of(level1Word, level2Word, level3Word));
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Level-specific matches")
    class LevelMatches {

        @Test
        @DisplayName("Level 1 sensitive word matched")
        void match_level1() {
            List<MatchedSensitiveWord> result = SensitiveWordMatcher.match(
                    "这是一个广告帖子", List.of(level1Word));
            assertEquals(1, result.size());
            assertEquals("广告", result.get(0).word());
            assertEquals(1, result.get(0).level());
        }

        @Test
        @DisplayName("Level 2 sensitive word matched")
        void match_level2() {
            List<MatchedSensitiveWord> result = SensitiveWordMatcher.match(
                    "售卖违禁品", List.of(level2Word));
            assertEquals(1, result.size());
            assertEquals("违禁品", result.get(0).word());
            assertEquals(2, result.get(0).level());
        }

        @Test
        @DisplayName("Level 3 sensitive word matched")
        void match_level3() {
            List<MatchedSensitiveWord> result = SensitiveWordMatcher.match(
                    "政治敏感内容", List.of(level3Word));
            assertEquals(1, result.size());
            assertEquals("政治敏感", result.get(0).word());
            assertEquals(3, result.get(0).level());
        }
    }

    @Nested
    @DisplayName("Multi-word matches")
    class MultiWordMatches {

        @Test
        @DisplayName("Multiple sensitive words matched in same text")
        void match_multipleWords() {
            List<MatchedSensitiveWord> result = SensitiveWordMatcher.match(
                    "这是一个广告，售卖违禁品，包含政治敏感内容",
                    List.of(level1Word, level2Word, level3Word));
            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("Risk level is max of all matched words")
        void riskLevel_isMax() {
            List<MatchedSensitiveWord> matches = SensitiveWordMatcher.match(
                    "广告和违禁品", List.of(level1Word, level2Word));
            int riskLevel = SensitiveWordMatcher.calculateRiskLevel(matches);
            assertEquals(2, riskLevel);
        }

        @Test
        @DisplayName("Risk level is 3 when level 3 word is present with others")
        void riskLevel_maxIs3() {
            List<MatchedSensitiveWord> matches = SensitiveWordMatcher.match(
                    "广告和违禁品和政治敏感", List.of(level1Word, level2Word, level3Word));
            int riskLevel = SensitiveWordMatcher.calculateRiskLevel(matches);
            assertEquals(3, riskLevel);
        }

        @Test
        @DisplayName("Risk level is 0 when no matches")
        void riskLevel_noMatch() {
            int riskLevel = SensitiveWordMatcher.calculateRiskLevel(List.of());
            assertEquals(0, riskLevel);
        }

        @Test
        @DisplayName("Risk level is 0 when null matches")
        void riskLevel_nullMatches() {
            int riskLevel = SensitiveWordMatcher.calculateRiskLevel(null);
            assertEquals(0, riskLevel);
        }
    }

    @Nested
    @DisplayName("Case normalization")
    class CaseNormalization {

        @Test
        @DisplayName("English text matched case-insensitively")
        void match_caseInsensitive() {
            SensitiveWordEntry word = new SensitiveWordEntry("spam", 1);
            List<MatchedSensitiveWord> result = SensitiveWordMatcher.match(
                    "This is SPAM content", List.of(word));
            assertEquals(1, result.size());
            assertEquals("spam", result.get(0).word());
        }

        @Test
        @DisplayName("Mixed case text matched correctly")
        void match_mixedCase() {
            SensitiveWordEntry word = new SensitiveWordEntry("BadWord", 2);
            List<MatchedSensitiveWord> result = SensitiveWordMatcher.match(
                    "some badword here", List.of(word));
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("Format matched words")
    class FormatMatchedWords {

        @Test
        @DisplayName("Null matched words returns null")
        void format_null() {
            assertNull(SensitiveWordMatcher.formatMatchedWords(null));
        }

        @Test
        @DisplayName("Empty matched words returns null")
        void format_empty() {
            assertNull(SensitiveWordMatcher.formatMatchedWords(List.of()));
        }

        @Test
        @DisplayName("Single matched word formatted correctly")
        void format_singleWord() {
            List<MatchedSensitiveWord> words = List.of(new MatchedSensitiveWord("广告", 1));
            assertEquals("广告", SensitiveWordMatcher.formatMatchedWords(words));
        }

        @Test
        @DisplayName("Multiple matched words comma-separated")
        void format_multipleWords() {
            List<MatchedSensitiveWord> words = List.of(
                    new MatchedSensitiveWord("广告", 1),
                    new MatchedSensitiveWord("违禁品", 2));
            assertEquals("广告,违禁品", SensitiveWordMatcher.formatMatchedWords(words));
        }

        @Test
        @DisplayName("Long matched words safely truncated at 500 chars")
        void format_safeTruncation() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                sb.append("很长的词");
            }
            String longWord = sb.toString();
            List<MatchedSensitiveWord> words = List.of(new MatchedSensitiveWord(longWord, 1));
            String result = SensitiveWordMatcher.formatMatchedWords(words);
            assertNotNull(result);
            assertTrue(result.length() <= 500);
        }
    }
}
