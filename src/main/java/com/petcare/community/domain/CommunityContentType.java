package com.petcare.community.domain;

/**
 * Content type constants for community posts and comments.
 * Used in content_review_record.content_type field.
 */
public final class CommunityContentType {

    private CommunityContentType() {
        // prevent instantiation
    }

    public static final String POST = "POST";
    public static final String COMMENT = "COMMENT";
}
