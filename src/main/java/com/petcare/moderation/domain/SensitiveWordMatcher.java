package com.petcare.moderation.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure logic for matching sensitive words against input text.
 * No database access — receives the active word list as input.
 *
 * Rules:
 * - null text is treated as empty string.
 * - Text is trimmed and lowercased for matching.
 * - Uses case-insensitive contains matching.
 * - Does NOT use regex (avoids injection from user-controlled words).
 * - Returns all matched words with their levels.
 */
public final class SensitiveWordMatcher {

    private static final int MAX_MATCHED_WORDS_LENGTH = 500;

    private SensitiveWordMatcher() {
        // prevent instantiation
    }

    /**
     * Matches sensitive words against the given text.
     *
     * @param text           the input text to check (may be null)
     * @param sensitiveWords the list of active sensitive words with levels
     * @return list of matched sensitive words (empty if no matches)
     */
    public static List<MatchedSensitiveWord> match(String text, List<SensitiveWordEntry> sensitiveWords) {
        if (sensitiveWords == null || sensitiveWords.isEmpty()) {
            return List.of();
        }

        String normalized = normalize(text);
        if (normalized.isEmpty()) {
            return List.of();
        }

        List<MatchedSensitiveWord> matches = new ArrayList<>();
        for (SensitiveWordEntry entry : sensitiveWords) {
            if (entry.word() == null || entry.word().isEmpty()) {
                continue;
            }
            String normalizedWord = entry.word().toLowerCase();
            if (normalized.contains(normalizedWord)) {
                matches.add(new MatchedSensitiveWord(entry.word(), entry.level()));
            }
        }
        return matches;
    }

    /**
     * Calculates the maximum risk level from matched words.
     *
     * @param matchedWords the list of matched sensitive words
     * @return the highest risk level, or 0 if no matches
     */
    public static int calculateRiskLevel(List<MatchedSensitiveWord> matchedWords) {
        if (matchedWords == null || matchedWords.isEmpty()) {
            return 0;
        }
        return matchedWords.stream()
                .mapToInt(MatchedSensitiveWord::level)
                .max()
                .orElse(0);
    }

    /**
     * Formats matched words into a comma-separated string for storage.
     * Safely truncates if the result exceeds the field limit.
     *
     * @param matchedWords the list of matched sensitive words
     * @return formatted string, safely truncated
     */
    public static String formatMatchedWords(List<MatchedSensitiveWord> matchedWords) {
        if (matchedWords == null || matchedWords.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matchedWords.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(matchedWords.get(i).word());
        }
        String result = sb.toString();
        if (result.length() > MAX_MATCHED_WORDS_LENGTH) {
            result = result.substring(0, MAX_MATCHED_WORDS_LENGTH);
        }
        return result;
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().toLowerCase();
    }

    /**
     * Represents an active sensitive word entry for matching.
     *
     * @param word  the sensitive word text
     * @param level the risk level (1-3)
     */
    public record SensitiveWordEntry(String word, int level) {
    }
}
