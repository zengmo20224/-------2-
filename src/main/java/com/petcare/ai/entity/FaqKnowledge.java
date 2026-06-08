package com.petcare.ai.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity for table `faq_knowledge`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("faq_knowledge")
public class FaqKnowledge extends BaseEntity {

    @TableField("question")
    private String question;

    @TableField("answer")
    private String answer;

    @TableField("category")
    private String category;

    @TableField("status")
    private String status;
}
