package com.petcare.moderation.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity for table `sensitive_word`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("sensitive_word")
public class SensitiveWord extends BaseEntity {

    @TableField("word")
    private String word;

    @TableField("category")
    private String category;

    @TableField("level")
    private Integer level;

    @TableField("status")
    private String status;
}
