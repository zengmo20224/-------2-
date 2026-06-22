package com.petcare.community.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity for table `community_tag`.
 * User-defined tags, created via # prefix in post content.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("community_tag")
public class CommunityTag extends BaseEntity {

    @TableField("name")
    private String name;

    @TableField("usage_count")
    private Integer usageCount;
}
