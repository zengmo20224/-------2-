package com.petcare.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity for table `user_security_question`.
 * Stores security questions and hashed answers for password recovery.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("user_security_question")
public class UserSecurityQuestion extends BaseEntity {

    @TableField("user_id")
    private Long userId;

    @TableField("question")
    private String question;

    @TableField("answer_hash")
    private String answerHash;

    @TableField("sort")
    private Integer sort;
}
