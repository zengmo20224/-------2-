package com.petcare.community.enums;

import lombok.Getter;

/**
 * Content status constants for posts and comments.
 * Default: PUBLISHED (matches schema.sql `post.status` default)
 */
@Getter
public enum ContentStatus {
    PUBLISHED("PUBLISHED"),
    PENDING_REVIEW("PENDING_REVIEW"),
    REJECTED("REJECTED"),
    HIDDEN("HIDDEN"),
    DELETED("DELETED");

    private final String code;

    ContentStatus(String code) {
        this.code = code;
    }
}
