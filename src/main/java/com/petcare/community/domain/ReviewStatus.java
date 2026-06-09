package com.petcare.community.domain;

/**
 * Review status constants for content moderation.
 * Used in content_review_record.review_status field.
 */
public final class ReviewStatus {

    private ReviewStatus() {
        // prevent instantiation
    }

    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";
}
