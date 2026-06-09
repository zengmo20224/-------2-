package com.petcare.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a post.
 * userId is NOT accepted from the request body — it comes from the security context.
 */
public record PostCreateRequest(
        Long topicId,
        Long petId,
        @NotBlank(message = "帖子标题不能为空")
        @Size(min = 1, max = 120, message = "帖子标题长度需要在1到120之间")
        String title,
        @NotBlank(message = "帖子内容不能为空")
        @Size(min = 1, max = 5000, message = "帖子内容长度需要在1到5000之间")
        String content
) {
}
