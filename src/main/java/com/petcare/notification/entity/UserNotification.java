package com.petcare.notification.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity for table `user_notification`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("user_notification")
public class UserNotification extends BaseEntity {

    @TableField("user_id")
    private Long userId;

    @TableField("actor_id")
    private Long actorId;

    @TableField("type")
    private String type;

    @TableField("post_id")
    private Long postId;

    @TableField("comment_id")
    private Long commentId;

    @TableField("content")
    private String content;

    @TableField("is_read")
    private Integer isRead;
}
