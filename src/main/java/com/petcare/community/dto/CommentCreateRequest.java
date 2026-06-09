package com.petcare.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a comment.
 * userId is NOT accepted from the request body — it comes from the security context.
 */
public record CommentCreateRequest(
        Long parentId,
        @NotBlank(message = "评论内容不能为空")
        @Size(min = 1, max = 1000, message = "评论内容长度需要在1到1000之间")
        String content
) {
}
