package com.petcare.community.entity;

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
 * Entity for table `post_report`.
 * Does NOT extend BaseEntity because this table has no update_time or deleted field.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("post_report")
public class PostReport {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("post_id")
    private Long postId;

    @TableField("reporter_id")
    private Long reporterId;

    @TableField("reason_type")
    private String reasonType;

    @TableField("reason")
    private String reason;

    @TableField("status")
    private String status;

    @TableField("handle_result")
    private String handleResult;

    @TableField("handler_id")
    private Long handlerId;

    @TableField("handle_time")
    private LocalDateTime handleTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
