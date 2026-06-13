package com.petcare.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

/**
 * Response for forgot-password step 1: returns the user's security questions.
 */
public record SecurityQuestionView(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        String question
) {}
