package com.petcare.ai.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity for table `ai_conversation`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("ai_conversation")
public class AiConversation extends BaseEntity {

    @TableField("user_id")
    private Long userId;

    @TableField("admin_id")
    private Long adminId;

    @TableField("conversation_type")
    private String conversationType;

    @TableField("title")
    private String title;
}
