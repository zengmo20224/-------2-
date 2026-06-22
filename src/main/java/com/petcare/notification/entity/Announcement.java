package com.petcare.notification.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity for table `announcement`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("announcement")
public class Announcement extends BaseEntity {

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("status")
    private String status;

    @TableField("sort")
    private Integer sort;
}
