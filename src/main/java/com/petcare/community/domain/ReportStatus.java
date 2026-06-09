package com.petcare.community.domain;

/**
 * Report status constants for post reports.
 * Used in post_report.status field.
 */
public final class ReportStatus {

    private ReportStatus() {
        // prevent instantiation
    }

    public static final String PENDING = "PENDING";
    public static final String PROCESSED = "PROCESSED";
    public static final String IGNORED = "IGNORED";
}
