package com.petcare.moderation.domain;

/**
 * Represents a sensitive word that was matched in content.
 * Immutable value object.
 */
public record MatchedSensitiveWord(String word, int level) {
}
