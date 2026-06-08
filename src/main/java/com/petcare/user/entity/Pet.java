package com.petcare.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entity for table `pet`.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("pet")
public class Pet extends BaseEntity {

    @TableField("user_id")
    private Long userId;

    @TableField("name")
    private String name;

    @TableField("type")
    private String type;

    @TableField("breed")
    private String breed;

    @TableField("gender")
    private Integer gender;

    @TableField("age")
    private BigDecimal age;

    @TableField("weight")
    private BigDecimal weight;

    @TableField("size")
    private String size;

    @TableField("sterilized")
    private Integer sterilized;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("remark")
    private String remark;
}
