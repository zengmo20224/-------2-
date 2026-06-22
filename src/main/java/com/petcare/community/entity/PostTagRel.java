package com.petcare.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity for table `post_tag_rel`.
 * Many-to-many relation between posts and community tags.
 * No logical delete — rows are physically removed when tags change.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("post_tag_rel")
public class PostTagRel {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("post_id")
    private Long postId;

    @TableField("tag_id")
    private Long tagId;

    @TableField(value = "create_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;
}
