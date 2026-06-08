package com.petcare.moderation.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity for table `content_review_record`.
 * Does NOT extend BaseEntity because this table has no update_time or deleted field.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("content_review_record")
public class ContentReviewRecord {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("content_type")
    private String contentType;

    @TableField("content_id")
    private Long contentId;

    @TableField("user_id")
    private Long userId;

    @TableField("risk_level")
    private Integer riskLevel;

    @TableField("matched_words")
    private String matchedWords;

    @TableField("review_status")
    private String reviewStatus;

    @TableField("reviewer_id")
    private Long reviewerId;

    @TableField("review_remark")
    private String reviewRemark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField("review_time")
    private LocalDateTime reviewTime;
}
