package com.petcare.community.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity for table `post_comment`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("post_comment")
public class PostComment extends BaseEntity {

    @TableField("post_id")
    private Long postId;

    @TableField("user_id")
    private Long userId;

    @TableField("parent_id")
    private Long parentId;

    @TableField("content")
    private String content;

    @TableField("status")
    private String status;

    @TableField("risk_level")
    private Integer riskLevel;

    @TableField("like_count")
    private Integer likeCount;
}
