package com.petcare.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcare.common.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity for table `admin_user`.
 * Password field is excluded from toString to prevent leakage.
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("admin_user")
public class AdminUser extends BaseEntity {

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("nickname")
    private String nickname;

    @TableField("phone")
    private String phone;

    @TableField("role")
    private String role;

    @TableField("status")
    private String status;

    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    @Override
    public String toString() {
        return "AdminUser(id=" + getId()
                + ", username=" + username
                + ", nickname=" + nickname
                + ", phone=" + phone
                + ", role=" + role
                + ", status=" + status
                + ", lastLoginTime=" + lastLoginTime
                + ", createTime=" + getCreateTime()
                + ", updateTime=" + getUpdateTime()
                + ")";
    }
}
