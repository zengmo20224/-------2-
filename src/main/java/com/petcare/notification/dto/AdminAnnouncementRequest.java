package com.petcare.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Admin create/update announcement request.
 */
public record AdminAnnouncementRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String content,
        String status,
        Integer sort
) {
}
